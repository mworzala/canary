package com.mattworzala.canary.internal.junit.execution;

import com.mattworzala.canary.internal.execution.TestExecutionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

public class JUnitTestExecutionListenerAdapter implements TestExecutionListener {
    private final EngineExecutionListener delegate;

    public JUnitTestExecutionListenerAdapter(EngineExecutionListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void start(@NotNull TestDescriptor descriptor) {
        delegate.executionStarted(descriptor);
    }

    @Override
    public void end(@NotNull TestDescriptor descriptor, @Nullable Throwable failure) {
        if (failure == null) {
            delegate.executionFinished(descriptor, TestExecutionResult.successful());
        } else {
            delegate.executionFinished(descriptor, TestExecutionResult.failed(failure));
        }
    }
}
