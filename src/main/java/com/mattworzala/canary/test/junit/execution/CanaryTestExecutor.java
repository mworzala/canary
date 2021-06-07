package com.mattworzala.canary.test.junit.execution;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

public class CanaryTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CanaryTestExecutor.class);

    private final EngineExecutionListener listener;

    public CanaryTestExecutor(EngineExecutionListener listener) {
        this.listener = listener;
    }

    public void execute(TestDescriptor test) {
        logger.info(() -> test.getUniqueId().toString());
        listener.executionStarted(test);



        //todo actual execution

        for (TestDescriptor child : test.getChildren()) {
            execute( child);
        }

        listener.executionFinished(test, TestExecutionResult.successful());
//        listener.executionFinished(test, TestExecutionResult.failed(new RuntimeException()));
    }
}
