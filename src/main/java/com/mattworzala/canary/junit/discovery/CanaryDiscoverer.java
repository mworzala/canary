package com.mattworzala.canary.junit.discovery;

import com.mattworzala.canary.junit.CanaryTestEngine;
import com.mattworzala.canary.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.junit.descriptor.RunnerTestDescriptor;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

/**
 * Very similar to the Junit Vantage discoverer
 */
public class CanaryDiscoverer {
    private static final IsPotentialCanaryTestClass isPotentialCanaryTestClass = new IsPotentialCanaryTestClass();

    private static final EngineDiscoveryRequestResolver<TestDescriptor> resolver = EngineDiscoveryRequestResolver.builder()
            .addClassContainerSelectorResolver(isPotentialCanaryTestClass)
            .addSelectorResolver(context -> new ClassSelectorResolver(ClassFilter.of(context.getClassNameFilter(), isPotentialCanaryTestClass)))
            .addSelectorResolver(new MethodSelectorResolver())
            .build();

    public EngineDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        CanaryEngineDescriptor engineDescriptor = new CanaryEngineDescriptor(uniqueId);
        resolver.resolve(discoveryRequest, engineDescriptor);
        TestDescriptorPostProcessor postProcessor = new TestDescriptorPostProcessor();
        for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
            RunnerTestDescriptor runnerDescriptor = (RunnerTestDescriptor) testDescriptor;
            postProcessor.applyFiltersAndCreateDescendants(runnerDescriptor);
        }

        return engineDescriptor;
    }
}
