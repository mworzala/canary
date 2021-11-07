package com.mattworzala.canary.platform.junit;

import com.mattworzala.canary.platform.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.platform.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.platform.util.safety.EnvType;
import com.mattworzala.canary.platform.util.safety.Env;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.util.Collection;

@Env(EnvType.PLATFORM)
public interface TestDescriptorVisitor {

    default boolean visitTestClass(@NotNull CanaryTestDescriptor test, @NotNull ClassSource source) {
        return false;
    }

    default boolean visitTestMethod(@NotNull CanaryTestDescriptor test, @NotNull MethodSource source) {
        return false;
    }

    default void visit(@NotNull CanaryEngineDescriptor descriptor) {
        visitChildren(descriptor.getChildrenMutable());
    }

    default boolean visit(@NotNull CanaryTestDescriptor test, @NotNull TestSource testSource) {
        // Visit this test
        boolean remove = false;
        if (testSource instanceof ClassSource classSource) {
            remove = visitTestClass(test, classSource);
        } else if (testSource instanceof MethodSource methodSource) {
            remove = visitTestMethod(test, methodSource);
        }

        // Visit children
        if (!remove)
            visitChildren(test.getChildrenMutable());

        return remove;
    }

    @ApiStatus.Internal
    default void visitChildren(Collection<TestDescriptor> children) {
        var iter = children.iterator();
        while (iter.hasNext()) {
            var testDescriptor = iter.next();
            var testSource = testDescriptor.getSource().orElse(null);
            // If not a canary test or it has no source (an error somewhere), we dont want it.
            if (!(testDescriptor instanceof CanaryTestDescriptor test) || testSource == null) {
                iter.remove();
                continue;
            }

            if (visit(test, testSource))
                iter.remove();
        }
    }
}
