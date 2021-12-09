package com.mattworzala.canary.internal.junit.discovery;

import com.mattworzala.canary.internal.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.internal.util.ClassLoaders;
import com.mattworzala.canary.internal.util.safety.Env;
import com.mattworzala.canary.internal.util.safety.EnvType;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import static com.mattworzala.canary.internal.junit.discovery.DiscoveryPostProcessor.*;
import static com.mattworzala.canary.internal.util.ClassLoaders.loadClassRequired;
import static com.mattworzala.canary.internal.util.ReflectionUtils.hasNoParameters;
import static com.mattworzala.canary.internal.util.ReflectionUtils.hasParameterTypes;
import static org.junit.platform.commons.util.ReflectionUtils.*;

@Env(EnvType.PLATFORM)
public class CanaryDiscoverer {
    //TODO: Discovery rework, we can use junit more effectively which will simplify and be more closely tied to junits rules
    // - org/junit/jupiter/engine/discovery/predicates/ has a bunch of predicates which can replace the ones defined here, or perhaps more likely just create my own version since theirs are internal
    // -


    // Ensure the class is public, not abstract, not an inner class (handled later) and contains at least one `InWorldTest` method.
    // We can filter out `InWorldTest` classes later, however, we are going to reload all selected classes into the minestom classloader in the next step, so better to filter more here.
    private static final Class<? extends Annotation> inWorldTestClass = com.mattworzala.canary.api.InWorldTest.class;
    public static final Predicate<Class<?>> IsPotentialTestClass = candidate -> isPublic(candidate) && !isAbstract(candidate) && isMethodPresent(candidate, method -> method.getAnnotation(inWorldTestClass) != null);
    public static final Predicate<Class<?>> IsPotentialTLTestClass = candidate -> IsPotentialTestClass.test(candidate) && !isInnerClass(candidate);     // Top level classes must be top level
    public static final Predicate<Class<?>> IsPotentialITestClass = candidate -> IsPotentialTestClass.test(candidate) && isStatic(candidate);           // Inner classes must be static

    // Ensure the method is not abstract, not static, and returns void. Other checks will be made (such as getting the `InWorldTest` annotation, however this is a prelim screening).
    private static final Class<?> testEnvironmentClass = loadClassRequired(ClassLoaders.MINESTOM, com.mattworzala.canary.api.TestEnvironment.class); //TODO: Its incredibly cursed that this class needs to be in Minestom CL wheras InWorldTest is in root.
    public static final Predicate<Method> IsPotentialTestMethod = candidate -> !isAbstract(candidate) && !isStatic(candidate) && returnsVoid(candidate) &&
                (hasNoParameters(candidate) || hasParameterTypes(candidate, testEnvironmentClass));

    private static final EngineDiscoveryRequestResolver<TestDescriptor> resolver = EngineDiscoveryRequestResolver.builder()
            .addClassContainerSelectorResolver(IsPotentialTLTestClass)
            .addSelectorResolver(context -> new ClassSelectorResolver(ClassFilter.of(context.getClassNameFilter(), IsPotentialTLTestClass)))
            .build();

    private static final DiscoveryPostProcessor postProcessor = new DiscoveryPostProcessor(
            new ResolveInnerClasses(),
            new ResolveMethods(),
            new PruneEmpty());

    public static CanaryEngineDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        // Root should always be an `EngineDescriptor`
        CanaryEngineDescriptor engineDescriptor = new CanaryEngineDescriptor(uniqueId);

        // Request all Potentially matching top level classes (inner classes will be discovered later)
        resolver.resolve(discoveryRequest, engineDescriptor);

        // Post processing
        postProcessor.execute(engineDescriptor);

        return engineDescriptor;
    }
}
