package com.mattworzala.canary.platform.junit.discovery;

import com.mattworzala.canary.api.IWAfterEach;
import com.mattworzala.canary.api.IWBeforeEach;
import com.mattworzala.canary.api.InWorldTest;
import com.mattworzala.canary.platform.junit.TestDescriptorVisitor;
import com.mattworzala.canary.platform.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.platform.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.platform.util.ClassLoaders;
import com.mattworzala.canary.platform.util.safety.EnvType;
import com.mattworzala.canary.platform.util.safety.Env;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.mattworzala.canary.platform.junit.discovery.CanaryDiscoverer.IsPotentialITestClass;
import static com.mattworzala.canary.platform.junit.discovery.CanaryDiscoverer.IsPotentialTestMethod;

@Env(EnvType.PLATFORM)
public record DiscoveryPostProcessor(TestDescriptorVisitor... processors) {

    public void execute(CanaryEngineDescriptor engineDescriptor) {
        for (var processor : processors())
            processor.visit(engineDescriptor);
    }

    /**
     * Resolves all inner classes which contain canary test cases.
     */
    @Env(EnvType.PLATFORM)
    public static class ResolveInnerClasses implements TestDescriptorVisitor {
        @Override
        public boolean visitTestClass(@NotNull CanaryTestDescriptor test, @NotNull ClassSource source) {
            Class<?> target = source.getJavaClass();

            // Only care about public classes contained
            for (Class<?> candidate : target.getClasses()) {
                if (candidate.getDeclaringClass() != target) continue;
                if (!IsPotentialITestClass.test(candidate)) continue;

                // Class is already in the minestom class loader, just add it as a child
                UniqueId childId = test.getUniqueId().append("class", candidate.getSimpleName());
                var child = new CanaryTestDescriptor(childId, candidate);
                test.addChild(child);
            }

            return false; // Do not remove anything
        }
    }

    /**
     * Resolves all canary test cases from the currently loaded classes.
     * Should be executed after {@link ResolveInnerClasses}.
     */
    @Env(EnvType.PLATFORM)
    public static class ResolveMethods implements TestDescriptorVisitor {
        private static final Class<? extends Annotation> inWorldTestAnnotation = ClassLoaders.loadAnnotation(ClassLoaders.MINESTOM, InWorldTest.class);
        private static final Class<? extends Annotation> iwBeforeEachAnnotation = ClassLoaders.loadAnnotation(ClassLoaders.MINESTOM, IWBeforeEach.class);
        private static final Class<? extends Annotation> iwAfterEachAnnotation = ClassLoaders.loadAnnotation(ClassLoaders.MINESTOM, IWAfterEach.class);

        static {
            assert inWorldTestAnnotation != null;
            assert iwBeforeEachAnnotation != null;
            assert iwAfterEachAnnotation != null;
        }

        @Override
        public boolean visitTestClass(@NotNull CanaryTestDescriptor test, @NotNull ClassSource source) {
            Class<?> testClass = source.getJavaClass();
            List<Method> preEffects = new ArrayList<>();
            List<Method> postEffects = new ArrayList<>();

            // Collect before/afterEach effects
            for (Method method : testClass.getDeclaredMethods()) {
                // Test basics such as modifiers, return type, and parameters
                if (!IsPotentialTestMethod.test(method)) continue;

                if (method.getAnnotation(iwBeforeEachAnnotation) != null) {
                    preEffects.add(method);
                }

                if (method.getAnnotation(iwAfterEachAnnotation) != null) {
                    if (preEffects.contains(method)) {
                        //todo logging? there is a junit logger that we could wrap to handle slf4j in sandbox
                        throw new RuntimeException("A @IWAfterEach method may not also be annotated with @IWBeforeEach.");
                    } else {
                        postEffects.add(method);
                    }
                }

            }

            // Search for `inWorldTest` methods
            for (Method method : testClass.getDeclaredMethods()) {
                // Test basics such as modifiers, return type, and parameters
                if (!IsPotentialTestMethod.test(method)) continue;

                Annotation inWorldTest = method.getAnnotation(inWorldTestAnnotation);
                if (inWorldTest == null) continue;
                if (preEffects.contains(method) || postEffects.contains(method)) {
                    //todo logging? there is a junit logger that we could wrap to handle slf4j in sandbox
                    throw new RuntimeException("An @InWorldTest method may not also be annotated with @IWBeforeEach or @IWAfterEach.");
                }

                String methodId = String.format("%s(%s)", method.getName(), ClassUtils.nullSafeToString(method.getParameterTypes()));
                UniqueId childId = test.getUniqueId().append("test", methodId);
                var child = new CanaryTestDescriptor(childId, method, preEffects, postEffects);
                test.addChild(child);
            }

            return false; // Do not remove anything
        }
    }

    /**
     * Removes any class descriptors which contain no test methods.
     */
    @Env(EnvType.PLATFORM)
    public static class PruneEmpty implements TestDescriptorVisitor {
        @Override
        public boolean visitTestClass(@NotNull CanaryTestDescriptor test, @NotNull ClassSource source) {
            // Remove if there are no children, this will propagate upwards since the children are visited first here.
            return test.getChildren().isEmpty();
        }

        /**
         * Visits children before the parent.
         *
         * @see TestDescriptorVisitor#visit(CanaryTestDescriptor, TestSource)
         */
        @Override
        public boolean visit(@NotNull CanaryTestDescriptor test, @NotNull TestSource testSource) {
            // Visit children
            visitChildren(test.getChildrenMutable());

            // Visit this test
            boolean remove = false;
            if (testSource instanceof ClassSource classSource) {
                remove = visitTestClass(test, classSource);
            } else if (testSource instanceof MethodSource methodSource) {
                remove = visitTestMethod(test, methodSource);
            }

            return remove;
        }
    }
}
