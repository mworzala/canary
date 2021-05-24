package com.mattworzala.canary.junit.discovery;

import com.mattworzala.canary.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.junit.descriptor.CanaryTestDescriptor;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

import java.util.function.Predicate;

import static org.junit.platform.commons.util.ReflectionUtils.isPublic;
import static org.junit.platform.commons.util.ReflectionUtils.isAbstract;
import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;

public class CanaryDiscoverer {
    private static final Logger logger = LoggerFactory.getLogger(CanaryDiscoverer.class);

//    private static final Predicate<Class<?>> isPotentialTestClass = candidate -> true;
    private static final Predicate<Class<?>> isPotentialTestClass = candidate -> isPublic(candidate) && !isAbstract(candidate) && !isInnerClass(candidate);

    private static final EngineDiscoveryRequestResolver<TestDescriptor> resolver = EngineDiscoveryRequestResolver.builder()
            .addClassContainerSelectorResolver(isPotentialTestClass)
            .addSelectorResolver(context -> new ClassSelectorResolver(ClassFilter.of(context.getClassNameFilter(), isPotentialTestClass)))
//            .addSelectorResolver(new MethodSelectorResolver())
            .build();

    public static CanaryEngineDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        logger.info(() -> "Discovering tests...");
        CanaryEngineDescriptor engineDescriptor = new CanaryEngineDescriptor(uniqueId);
        resolver.resolve(discoveryRequest, engineDescriptor);
        TestDescriptorPostProcessor postProcessor = new TestDescriptorPostProcessor(isPotentialTestClass);
        for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
            postProcessor.process((CanaryTestDescriptor) testDescriptor);
        }
        return engineDescriptor;
    }
}
