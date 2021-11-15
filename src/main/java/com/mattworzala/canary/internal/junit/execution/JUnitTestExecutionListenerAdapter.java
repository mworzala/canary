package com.mattworzala.canary.internal.junit.execution;

import com.mattworzala.canary.internal.execution.TestExecutionListener;
import com.mattworzala.canary.internal.util.safety.Env;
import com.mattworzala.canary.internal.util.safety.EnvType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

@Env(EnvType.GLOBAL)
public class JUnitTestExecutionListenerAdapter implements TestExecutionListener {
    private final EngineExecutionListener delegate;

    public JUnitTestExecutionListenerAdapter(EngineExecutionListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void start(@NotNull TestDescriptor descriptor) {
        System.out.println("STARTING " + descriptor.getDisplayName());
        delegate.executionStarted(descriptor);
    }

    @Override
    public void end(@NotNull TestDescriptor descriptor, @Nullable Throwable failure) {
        System.out.println("ENDING " + descriptor.getDisplayName());
        if (failure == null) {
            delegate.executionFinished(descriptor, TestExecutionResult.successful());
        } else {
            delegate.executionFinished(descriptor, TestExecutionResult.failed(failure));
        }
    }
}
