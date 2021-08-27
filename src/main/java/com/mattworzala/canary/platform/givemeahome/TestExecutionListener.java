package com.mattworzala.canary.platform.givemeahome;

import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestDescriptor;

import javax.annotation.Nullable;

public interface TestExecutionListener {

    //todo this should be a CanaryTestDescriptor eventually
    void start(@NotNull TestDescriptor descriptor);

    default void end(@NotNull TestDescriptor descriptor) { end(descriptor, null); }

    void end(@NotNull TestDescriptor descriptor, @Nullable Throwable failure);
}
