package com.mattworzala.canary.server.execution;

import com.mattworzala.canary.platform.givemeahome.TestExecutionListener;
import com.mattworzala.canary.platform.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.platform.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.server.instance.BasicGenerator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

// 1 per server, keeps track of each TestExecutor to run tests given to it, also manages creation and destruction of TestInstances.
//   Executing tests here will be a blocking operation (CanaryTestEngine#execute will be replaced with a single call here)
//   Returns when all test results have been reported
public class TestCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(TestCoordinator.class);

    private final Predicate<TestDescriptor> systemTestFilter = descriptor -> true;

    private CanaryEngineDescriptor engineDescriptor;
    private final Map<UniqueId, TestExecutor> executors = new HashMap<>();

    private final InstanceContainer instance;

    public TestCoordinator() {
        instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkGenerator(new BasicGenerator());

        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                instance.loadChunk(x, z).join();
            }
        }
    }

    public InstanceContainer getInstance() {
        return instance;
    }

    public void indexTests(CanaryEngineDescriptor descriptor) {
        this.engineDescriptor = descriptor;

        indexTestsRecursive(descriptor);
        //todo index
    }

    private Point lastTestOrigin = new Vec(2, 41, 0);

    private void indexTestsRecursive(TestDescriptor descriptor) {
        TestSource source = descriptor.getSource().orElse(null);
        if (source instanceof MethodSource) {
            var executor = new TestExecutor((CanaryTestDescriptor) descriptor, instance, lastTestOrigin);
            executors.put(descriptor.getUniqueId(), executor);
            lastTestOrigin = lastTestOrigin.withZ(z -> z + executor.getStructure().getSizeZ() + 5);
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
