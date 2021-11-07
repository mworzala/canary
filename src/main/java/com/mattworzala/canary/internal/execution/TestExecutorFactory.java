package com.mattworzala.canary.internal.execution;

import com.mattworzala.canary.internal.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.internal.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.internal.server.instance.BasicGenerator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestExecutorFactory {
    private static final int INITIAL_X = 5;
    private static final int BORDER_AREA = 5;

    private final Map<UniqueId, TestExecutor> output;

    private Point nextValidPosition = new Vec(INITIAL_X, 41, 0);
    private int currentMaxSize = 0;

    public TestExecutorFactory(Map<UniqueId, TestExecutor> output) {
        this.output = output;
    }


    public void createExecutors(CanaryEngineDescriptor engineDescriptor) {
        engineDescriptor.getChildren().forEach(this::createExecutorRecursive);
    }

    public void createExecutorRecursive(TestDescriptor descriptor) {
        TestSource source = descriptor.getSource().orElse(null);
        if (source == null) {
            return;
        }

        if (source instanceof ClassSource) {
            List<TestDescriptor> childTestContainers = new ArrayList<>();

            // Create child executors, holding off on child classes
            for (TestDescriptor child : descriptor.getChildren()) {
                TestSource childSource = child.getSource().orElse(null);

                if (childSource instanceof MethodSource) {
                    // Create executor
                    TestExecutor executor = createExecutor((CanaryTestDescriptor) child);
                    output.put(child.getUniqueId(), executor);

                } else if (childSource instanceof ClassSource) {
                    // Index after this test
                    childTestContainers.add(child);
                }
            }

            // Increment to next area
            nextValidPosition = new Vec(INITIAL_X, 41, nextValidPosition.z() + currentMaxSize + BORDER_AREA);
            currentMaxSize = 0;

            // Index children
            childTestContainers.forEach(this::createExecutorRecursive);
        }
    }

    private TestExecutor createExecutor(CanaryTestDescriptor descriptor) {
        var testInstance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        MinecraftServer.getInstanceManager().registerInstance(testInstance);
        testInstance.setChunkGenerator(new BasicGenerator());

        var executor = new TestExecutor(descriptor, testInstance, nextValidPosition);
        // Increment depth
        nextValidPosition = nextValidPosition.withX(x -> x + executor.getStructure().getSizeX() + BORDER_AREA);
        currentMaxSize = Math.max(currentMaxSize, executor.getStructure().getSizeZ());

        return executor;
    }
}
