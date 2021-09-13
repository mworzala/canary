package com.mattworzala.canary.server.givemeahome.v2;

import com.mattworzala.canary.platform.givemeahome.TestExecutionListener;
import com.mattworzala.canary.platform.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.platform.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.server.givemeahome.TestExecutor;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class TestCoordinatorV2 {
    private static final Logger logger = LoggerFactory.getLogger(TestCoordinatorV2.class);

    private final Predicate<TestDescriptor> systemTestFilter = descriptor -> true;

    private CanaryEngineDescriptor engineDescriptor;
    private final Map<UniqueId, TestExecutor> executors = new HashMap<>();

    public void indexTests(CanaryEngineDescriptor descriptor) {
        this.engineDescriptor = descriptor;

        indexTestsRecursive(descriptor);
        //todo index
    }

    private void indexTestsRecursive(TestDescriptor descriptor) {
        TestSource source = descriptor.getSource().orElse(null);
        if (source instanceof MethodSource) {
            executors.put(descriptor.getUniqueId(), new TestExecutor((CanaryTestDescriptor) descriptor));
        }

        descriptor.getChildren().forEach(this::indexTestsRecursive);
    }

    public void execute(TestExecutionListener listener) {
        //todo parallel execution
        listener.start(engineDescriptor);

        for (TestDescriptor descriptor : engineDescriptor.getChildren()) {
            executeRecursive(listener, descriptor);
        }

        listener.end(engineDescriptor);

        engineDescriptor.getChildrenMutable().clear();
    }

    private void executeRecursive(TestExecutionListener listener, TestDescriptor descriptor) {
        boolean hasDedicatedExecutor = executors.containsKey(descriptor.getUniqueId());
        if (!hasDedicatedExecutor) {
            // Just execute child
            listener.start(descriptor);

            for (TestDescriptor child : descriptor.getChildren()) {
                executeRecursive(listener, child);
            }

            listener.end(descriptor);
        } else {
            if (!systemTestFilter.test(descriptor)) {
                logger.info("Skipping {} due to filtering", descriptor.getUniqueId());
                return;
            }

            TestExecutor executor = executors.get(descriptor.getUniqueId());
            executor.execute(listener); // non-blocking (starts execution)

            // temp wait until execution finished
            while (executor.isRunning());
        }
    }
}
