package com.mattworzala.canary.junit.discovery;

import com.mattworzala.canary.junit.descriptor.RunnerTestDescriptor;
import com.mattworzala.canary.junit.runner.Runner;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.mattworzala.canary.junit.descriptor.CanaryTestDescriptor.SEGMENT_TYPE_RUNNER;

public class ClassSelectorResolver implements SelectorResolver {
    private static final RunnerBuilder RUNNER_BUILDER = new DefensiveAllDefaultPossibilitiesBuilder();

    private final ClassFilter filter;

    public ClassSelectorResolver(@NotNull ClassFilter filter) {
        this.filter = filter;
    }

    @Override
    public Resolution resolve(ClassSelector selector, Context context) {
        return resolveTestCase(selector.getJavaClass(), context);
    }

    @Override
    public Resolution resolve(UniqueIdSelector selector, Context context) {
        Segment lastSegment = selector.getUniqueId().getLastSegment();
        if (SEGMENT_TYPE_RUNNER.equals(lastSegment.getType())) {
            String testClassName = lastSegment.getValue();
            ReflectionUtils.tryToLoadClass(testClassName)
                    .getOrThrow(e -> new JUnitException("Unknown class: " + testClassName, e));
        }
        return Resolution.unresolved();
    }

    private Resolution resolveTestCase(Class<?> testClass, Context context) {
        if (!filter.test(testClass))
            return Resolution.unresolved();

        Runner runner = RUNNER_BUILDER.safeRunnerForClass(testClass);
        if (runner == null) {
            return Resolution.unresolved();
        }
        return context.addToParent(parent -> Optional.of(createRunnerTestDescriptor(parent, testClass, runner))).map(
                runnerTestDescriptor -> Match.exact(runnerTestDescriptor, () -> {
                    runnerTestDescriptor.clearFilters();
                    return Collections.emptySet();
                })).map(Resolution::match).orElse(Resolution.unresolved());
    }

    private RunnerTestDescriptor createRunnerTestDescriptor(TestDescriptor parent, Class<?> testClass, Runner runner) {
        UniqueId uniqueId = parent.getUniqueId().append(SEGMENT_TYPE_RUNNER, testClass.getName());
        return new RunnerTestDescriptor(uniqueId, testClass, runner);
    }
}
