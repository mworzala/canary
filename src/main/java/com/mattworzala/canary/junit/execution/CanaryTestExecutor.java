package com.mattworzala.canary.junit.execution;

import com.mattworzala.canary.junit.descriptor.CanaryTestDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

public class CanaryTestExecutor {
    private final EngineExecutionListener listener;

    public CanaryTestExecutor(EngineExecutionListener listener) {
        this.listener = listener;
    }

    public void execute(CanaryTestDescriptor test) {
        listener.executionStarted(test);



        //todo actual execution

        for (TestDescriptor child : test.getChildren()) {
            execute((CanaryTestDescriptor) child);
        }

        listener.executionFinished(test, TestExecutionResult.successful());
//        listener.executionFinished(test, TestExecutionResult.failed(new RuntimeException()));
    }
}
