package com.mattworzala.canary.platform;

import com.mattworzala.canary.platform.util.safety.EnvType;
import com.mattworzala.canary.platform.util.safety.Env;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestDescriptor;

import javax.annotation.Nullable;

@Env(EnvType.GLOBAL)
public interface TestExecutionListener {
    TestExecutionListener EMPTY = new TestExecutionListener() {
        public void start(@NotNull TestDescriptor descriptor) {}
        public void end(@NotNull TestDescriptor descriptor, @org.jetbrains.annotations.Nullable Throwable failure) {}
    };
    TestExecutionListener STDOUT = new TestExecutionListener() {
        @Override
        public void start(@NotNull TestDescriptor descriptor) {
            System.out.println("Starting " + descriptor.getDisplayName());
        }

        @Override
        public void end(@NotNull TestDescriptor descriptor, @org.jetbrains.annotations.Nullable Throwable failure) {
            if (failure == null) {
                System.out.println("Passed " + descriptor.getDisplayName());
            } else {
                System.out.println("Failed " + descriptor.getDisplayName() + ": " + failure.getMessage());
            }
        }
    };

    //todo this should be a CanaryTestDescriptor eventually
    void start(@NotNull TestDescriptor descriptor);

    default void end(@NotNull TestDescriptor descriptor) { end(descriptor, null); }

    void end(@NotNull TestDescriptor descriptor, @Nullable Throwable failure);
}
