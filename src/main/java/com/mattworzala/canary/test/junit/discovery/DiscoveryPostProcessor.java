package com.mattworzala.canary.test.junit.discovery;

import com.mattworzala.canary.test.junit.TestDescriptorVisitor;
import com.mattworzala.canary.test.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.test.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.test.junit.util.ClassLoaders;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static com.mattworzala.canary.test.junit.discovery.CanaryDiscoverer.*;

public record DiscoveryPostProcessor(TestDescriptorVisitor... processors) {

    public void execute(CanaryEngineDescriptor engineDescriptor) {
        for (var processor : processors())
            processor.visit(engineDescriptor);
    }

    /**
     * Resolves all inner classes which contain canary test cases.
     */
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
    public static class ResolveMethods implements TestDescriptorVisitor {
        private final Class<? extends Annotation> annotation = ClassLoaders.loadAnnotation(ClassLoaders.MINESTOM, "com.mattworzala.canary.test.InWorldTest");

        @Override
        public boolean visitTestClass(@NotNull CanaryTestDescriptor test, @NotNull ClassSource source) {
            Class<?> testClass = source.getJavaClass();

            // Inspect every method
            for (Method method : testClass.getDeclaredMethods()) {
                // Test basics such as modifiers, return type, and parameters
                if (!IsPotentialTestMethod.test(method)) continue;

                Annotation inWorldTest = method.getAnnotation(annotation);
                if (inWorldTest == null) continue;

                String methodId = String.format("%s(%s)", method.getName(), ClassUtils.nullSafeToString(method.getParameterTypes()));
                UniqueId childId = test.getUniqueId().append("test", methodId);
                var child = new CanaryTestDescriptor(childId, method);
                test.addChild(child);
            }

            return false; // Do not remove anything
        }
    }

    /**
     * Removes any class descriptors which contain no test methods.
     */
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
