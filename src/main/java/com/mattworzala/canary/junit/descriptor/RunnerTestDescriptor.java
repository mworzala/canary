package com.mattworzala.canary.junit.descriptor;

import com.mattworzala.canary.junit.runner.Filter;
import com.mattworzala.canary.junit.runner.Runner;
import com.mattworzala.canary.junit.runner.RunnerDecorator;
import com.mattworzala.canary.junit.support.Filterable;
import com.mattworzala.canary.junit.support.Request;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

import java.util.*;
import java.util.function.Consumer;

public class RunnerTestDescriptor extends CanaryTestDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(RunnerTestDescriptor.class);

    private final Set<TestDescription> rejectedExclusions = new HashSet<>();
    private Runner runner;
    private boolean wasFiltered;
    private List<Filter> filters = new ArrayList<>();

    public RunnerTestDescriptor(UniqueId uniqueId, Class<?> testClass, Runner runner) {
        super(uniqueId, runner.getDescription(), testClass.getSimpleName(), ClassSource.from(testClass));
        this.runner = runner;
    }

    @Override
    public String getLegacyReportingName() {
        return getSource().map(source -> ((ClassSource) source).getClassName()) //
                .orElseThrow(() -> new JUnitException("source should have been present"));
    }

    public Request toRequest() {
        return new RunnerRequest(this.runner);
    }

    @Override
    protected boolean tryToExcludeFromRunner(TestDescription description) {
        boolean excluded = tryToFilterRunner(description);
        if (excluded) {
            wasFiltered = true;
        }
        else {
            rejectedExclusions.add(description);
        }
        return excluded;
    }

    private boolean tryToFilterRunner(TestDescription description) {
        if (runner instanceof Filterable) {
            ExcludeDescriptionFilter filter = new ExcludeDescriptionFilter(description);
            try {
                ((Filterable) runner).filter(filter);
            } catch (Exception ignore) { //todo was NoTestsRemainingException
                // it's safe to ignore this exception because childless TestDescriptors will get pruned
            }
            return filter.wasSuccessful();
        }
        return false;
    }

    @Override
    protected boolean canBeRemovedFromHierarchy() {
        return true;
    }

    @Override
    public void prune() {
        if (wasFiltered) {
            // filtering the runner may render intermediate Descriptions obsolete
            // (e.g. test classes without any remaining children in a suite)
            pruneDescriptorsForObsoleteDescriptions(Collections.singletonList(runner.getDescription()));
        }
        if (rejectedExclusions.isEmpty()) {
            super.prune();
        }
        else if (rejectedExclusions.containsAll(getDescription().getChildren())) {
            // since the Runner was asked to remove all of its direct children,
            // it's safe to remove it entirely
            removeFromHierarchy();
        }
        else {
            logIncompleteFiltering();
        }
    }

    private void logIncompleteFiltering() {
        if (runner instanceof Filterable) {
            logger.warn(() -> "Runner " + getRunnerToReport().getClass().getName() //
                    + " (used on class " + getLegacyReportingName() + ") was not able to satisfy all filter requests.");
        }
        else {
            warnAboutUnfilterableRunner();
        }
    }

    private void warnAboutUnfilterableRunner() {
        logger.warn(() -> "Runner " + getRunnerToReport().getClass().getName() //
                + " (used on class " + getLegacyReportingName() + ") does not support filtering" //
                + " and will therefore be run completely.");
    }

    public Optional<List<Filter>> getFilters() {
        return Optional.ofNullable(filters);
    }

    public void clearFilters() {
        this.filters = null;
    }

    public void applyFilters(Consumer<RunnerTestDescriptor> childrenCreator) {
        if (filters != null && !filters.isEmpty()) {
            if (runner instanceof Filterable) {
                this.runner = toRequest().filterWith(new OrFilter(filters)).getRunner();
                this.description = runner.getDescription();
                this.children.clear();
                childrenCreator.accept(this);
            }
            else {
                warnAboutUnfilterableRunner();
            }
        }
        clearFilters();
    }

    private Runner getRunnerToReport() {
        return (runner instanceof RunnerDecorator) ? ((RunnerDecorator) runner).getDecoratedRunner() : runner;
    }

    private static class ExcludeDescriptionFilter extends Filter {

        private final TestDescription description;
        private boolean successful;

        ExcludeDescriptionFilter(TestDescription description) {
            this.description = description;
        }

        @Override
        public boolean shouldRun(TestDescription description) {
            if (this.description.equals(description)) {
                successful = true;
                return false;
            }
            return true;
        }

        @Override
        public String describe() {
            return "exclude " + description;
        }

        boolean wasSuccessful() {
            return successful;
        }
    }

}
