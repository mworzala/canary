package com.mattworzala.canary.internal.execution;

import com.mattworzala.canary.internal.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.internal.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.internal.server.instance.BasicGenerator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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
        instance = new InstanceContainer(new UUID(0, 0), DimensionType.OVERWORLD);
        MinecraftServer.getInstanceManager().registerInstance(instance);
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

    public CanaryEngineDescriptor getEngineDescriptor() {
        return engineDescriptor;
    }

    public TestExecutor getExecutor(@NotNull UniqueId uniqueId) {
        return executors.get(uniqueId);
    }

    public TestExecutor getExecutor(@NotNull CanaryTestDescriptor descriptor) {
        return executors.get(descriptor.getUniqueId());
    }

    // Called from platform implementation
    public void indexTests(CanaryEngineDescriptor descriptor) {
        this.engineDescriptor = descriptor;

        var factory = new TestExecutorFactory(this.executors);
        factory.createExecutors(descriptor);

        logger.info("Received {} executable tests", executors.size());

//        indexTestsRecursive(descriptor);
        //todo index
    }

//    private Point lastTestOrigin = new Vec(2, 41, 0);

//    private void indexTestsRecursive(TestDescriptor descriptor) {
//        TestSource source = descriptor.getSource().orElse(null);
//        if (source instanceof MethodSource) {
//            var testInstance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
//            MinecraftServer.getInstanceManager().registerInstance(testInstance);
//            testInstance.setChunkGenerator(new BasicGenerator());
//            var executor = new TestExecutor((CanaryTestDescriptor) descriptor, testInstance, lastTestOrigin);
//
//            {
//                // Add structure to sandbox instance as well
//                //TODO this does not handle resets
//                executor.getStructure().loadIntoBlockSetter(getInstance(), lastTestOrigin);
//            }
//
//            executors.put(descriptor.getUniqueId(), executor);
//            lastTestOrigin = lastTestOrigin.withZ(z -> z + executor.getStructure().getSizeZ() + 5);
//        }
//
//        descriptor.getChildren().forEach(this::indexTestsRecursive);
//    }

    public void execute(TestExecutionListener listener) {
        listener.start(engineDescriptor);

        CompletableFuture<Void> executionTask = CompletableFuture.allOf(
                engineDescriptor.getChildren().stream()
                        .map(child -> executeRecursive(listener, child, CompletableFuture.completedFuture(null)))
                        .toArray(CompletableFuture[]::new)
        );

        //todo do we want to block this thread?
        executionTask.join();

        listener.end(engineDescriptor);
    }

    private CompletableFuture<Void> executeRecursive(TestExecutionListener listener, TestDescriptor descriptor, CompletableFuture<Void> task) {
        CompletableFuture<Void> nextTask = CompletableFuture.completedFuture(null);
        boolean hasDedicatedExecutor = executors.containsKey(descriptor.getUniqueId());
        if (!hasDedicatedExecutor) {
            // Just execute children
            listener.start(descriptor);

            nextTask = CompletableFuture.allOf(
                    descriptor.getChildren().stream()
                            .map(child -> executeRecursive(listener, child, task))
                            .toArray(CompletableFuture[]::new)
            );

            nextTask = nextTask.thenAccept(v -> listener.end(descriptor));
        } else {
            if (!systemTestFilter.test(descriptor)) {
                logger.info("Skipping {} due to filtering", descriptor.getUniqueId());
                return nextTask;
            }

            TestExecutor executor = executors.get(descriptor.getUniqueId());
            CompletableFuture<Void> executeTask = new CompletableFuture<>();
            executor.execute(listener, executeTask); // non-blocking (starts execution)
            nextTask = nextTask.thenCompose(v -> executeTask);
        }

        return nextTask;
    }

    private void createExecutorsRecursive(TestDescriptor descriptor) {
        TestSource source = descriptor.getSource().orElse(null);
        if (source == null) return;

        if (source instanceof MethodSource) {
            //todo create descriptor
        }


    }
}
