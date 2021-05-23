package com.mattworzala.canary.junit.discovery;

import com.mattworzala.canary.junit.descriptor.RunnerTestDescriptor;
import com.mattworzala.canary.junit.descriptor.TestDescription;
import com.mattworzala.canary.junit.runner.Filter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.util.Optional;
import java.util.function.Function;

import static com.mattworzala.canary.junit.descriptor.CanaryTestDescriptor.SEGMENT_TYPE_RUNNER;

public class MethodSelectorResolver implements SelectorResolver {
    @Override
    public Resolution resolve(MethodSelector selector, Context context) {

        Class<?> testClass = selector.getJavaClass();
        return resolveParentAndAddFilter(context, DiscoverySelectors.selectClass(testClass), parent -> toMethodFilter(selector));
    }

    @Override
    public Resolution resolve(UniqueIdSelector selector, Context context) {
        for (UniqueId current = selector.getUniqueId(); !current.getSegments().isEmpty(); current = current.removeLastSegment()) {
            if (SEGMENT_TYPE_RUNNER.equals(current.getLastSegment().getType())) {
                return resolveParentAndAddFilter(context, DiscoverySelectors.selectUniqueId(current),
                        parent -> toUniqueIdFilter(parent, selector.getUniqueId()));
            }
        }
        return Resolution.unresolved();
    }

    private Resolution resolveParentAndAddFilter(Context context, DiscoverySelector selector,
                                                 Function<RunnerTestDescriptor, Filter> filterCreator) {
        return context.resolve(selector).flatMap(parent -> addFilter(parent, filterCreator)).map(
                this::toResolution).orElse(Resolution.unresolved());
    }

    private Optional<RunnerTestDescriptor> addFilter(TestDescriptor parent,
                                                     Function<RunnerTestDescriptor, Filter> filterCreator) {
        if (parent instanceof RunnerTestDescriptor) {
            RunnerTestDescriptor runnerTestDescriptor = (RunnerTestDescriptor) parent;
            runnerTestDescriptor.getFilters().ifPresent(
                    filters -> filters.add(filterCreator.apply(runnerTestDescriptor)));
            return Optional.of(runnerTestDescriptor);
        }
        return Optional.empty();
    }

    private Resolution toResolution(RunnerTestDescriptor parent) {
        return Resolution.match(Match.partial(parent));
    }

    private Filter toMethodFilter(MethodSelector methodSelector) {
        Class<?> testClass = methodSelector.getJavaClass();
        String methodName = methodSelector.getMethodName();
        return matchMethodDescription(TestDescription.createTestDescription(testClass, methodName));
    }

    private Filter toUniqueIdFilter(RunnerTestDescriptor runnerTestDescriptor, UniqueId uniqueId) {
        return new UniqueIdFilter(runnerTestDescriptor, uniqueId);
    }

    private static Filter matchMethodDescription(final TestDescription desiredDescription) {
        String desiredMethodName = TestDescription.getMethodName(desiredDescription);
        return new Filter() {

            @Override
            public boolean shouldRun(TestDescription description) {
                if (description.isTest()) {
                    return desiredDescription.equals(description) || isParameterizedMethod(description);
                }

                // explicitly check if any children want to run
                for (TestDescription each : description.getChildren()) {
                    if (shouldRun(each)) {
                        return true;
                    }
                }
                return false;
            }

            private boolean isParameterizedMethod(TestDescription description) {
                String methodName = DescriptionUtils.getMethodName(description);
                return methodName.startsWith(desiredMethodName + "[");
            }

            @Override
            public String describe() {
                return String.format("Method %s", desiredDescription.getDisplayName());
            }
        };
    }
}
