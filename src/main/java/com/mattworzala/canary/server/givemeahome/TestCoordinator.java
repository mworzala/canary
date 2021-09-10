package com.mattworzala.canary.server.givemeahome;

import com.mattworzala.canary.platform.givemeahome.TestExecutionListener;
import com.mattworzala.canary.platform.junit.TestDescriptorVisitor;
import com.mattworzala.canary.platform.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.platform.junit.descriptor.CanaryTestDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.util.HashMap;
import java.util.Map;

// 1 per server, keeps track of each TestExecutor to run tests given to it, also manages creation and destruction of TestInstances.
//   Executing tests here will be a blocking operation (CanaryTestEngine#execute will be replaced with a single call here)
//   Returns when all test results have been reported
public class TestCoordinator {

    public boolean isHeadless() {
        return true;
    }

    private final CanaryEngineDescriptor root;
    private final Map<CanaryTestDescriptor, TestExecutor> executors = new HashMap<>();

    private volatile boolean running = false;

    public TestCoordinator(@NotNull CanaryEngineDescriptor engineDescriptor) {
        this.root = engineDescriptor;

        new Visitor().visit(engineDescriptor);
    }

    public boolean isRunning() {
        return running;
    }

    public CanaryEngineDescriptor getRootDescriptor() {
        return root;
    }

    public TestExecutor getExecutor(CanaryTestDescriptor descriptor) {
        return executors.get(descriptor);
    }

    /**
     * Blocking call to execute some tests and report their outcome to the provided listener.
     *
     * @param descriptor
     */
    public void execute(TestDescriptor descriptor, TestExecutionListener listener) {
        //todo this method doesnt handle filtering well. We may want to define a separate filter and then just always execute every matching test.
        //     that would handle the classloader split problem as well.
        if (running) {
            throw new IllegalStateException("Cannot execute multiple test batches at the same time.");
        }


    }

    private void doBlockingExecution() {
        //todo for now just execute all


    }

    private class Visitor implements TestDescriptorVisitor {
        @Override
        public boolean visitTestMethod(@NotNull CanaryTestDescriptor test, @NotNull MethodSource source) {
            executors.put(test, new TestExecutor(test));

            return TestDescriptorVisitor.super.visitTestMethod(test, source);
        }
    }

}
