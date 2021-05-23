package com.mattworzala.canary.junit.execution;

import com.mattworzala.canary.junit.descriptor.RunnerTestDescriptor;
import com.mattworzala.canary.junit.descriptor.TestSourceProvider;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;

public class RunnerExecutor {

    private final EngineExecutionListener engineExecutionListener;
    private final TestSourceProvider testSourceProvider = new TestSourceProvider();

    public RunnerExecutor(EngineExecutionListener engineExecutionListener) {
        this.engineExecutionListener = engineExecutionListener;
    }

    public void execute(RunnerTestDescriptor runnerTestDescriptor) {
        TestRun testRun = new TestRun(runnerTestDescriptor);
        JUnitCore core = new JUnitCore();
        core.addListener(new RunListenerAdapter(testRun, engineExecutionListener, testSourceProvider));
        try {
            core.run(runnerTestDescriptor.toRequest());
        } catch (Throwable t) {
            throw new RuntimeException(t); //todo
//            UnrecoverableExceptions.rethrowIfUnrecoverable(t);
//            reportUnexpectedFailure(testRun, runnerTestDescriptor, TestExecutionResult.failed(t));
        }
    }

    private void reportUnexpectedFailure(TestRun testRun, RunnerTestDescriptor runnerTestDescriptor,
                                         TestExecutionResult result) {
        if (testRun.isNotStarted(runnerTestDescriptor)) {
            engineExecutionListener.executionStarted(runnerTestDescriptor);
        }
        engineExecutionListener.executionFinished(runnerTestDescriptor, result);
    }

}
