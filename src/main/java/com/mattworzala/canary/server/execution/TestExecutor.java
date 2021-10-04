package com.mattworzala.canary.server.execution;

import com.mattworzala.canary.platform.givemeahome.TestExecutionListener;
import com.mattworzala.canary.platform.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.server.assertion.AssertionImpl;
import com.mattworzala.canary.server.env.TestEnvironmentImpl;
import com.mattworzala.canary.server.givemeahome.BoundingBoxHandler;
import com.mattworzala.canary.server.structure.JsonStructureIO;
import com.mattworzala.canary.server.structure.Structure;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.mattworzala.canary.platform.util.ReflectionUtils.invokeMethodOptionalParameter;

// one per test method (reused), handles instantiating the test class, invoking the before/run/after methods, cleaning up the test for the next execution (replace structure).
//   Executing a test is not blocking, it must be ticked until it reports that it has a result.
public class TestExecutor implements Tickable {
    private static final EventFilter<InstanceTickEvent, Instance> FILTER_INSTANCE_TICK = EventFilter.from(InstanceTickEvent.class, Instance.class, InstanceTickEvent::getInstance);
    private static final EventNode<InstanceTickEvent> TICK_NODE = EventNode.type("EventExecutor_InstanceTick", FILTER_INSTANCE_TICK);

    private static final JsonStructureIO structureIo = new JsonStructureIO();

    static {
        MinecraftServer.getGlobalEventHandler().addChild(TICK_NODE);
    }

    private final CanaryTestDescriptor testDescriptor;

    private int executionCount = 0;
    private final Instance instance;
    private final Structure structure;
    private final Point origin;

    // Mid-test state (anything which is accumulated while running a test)
    private volatile boolean running;
    private TestExecutionListener executionListener;
    private Object classInstance;
    private final List<AssertionImpl<?, ?>> assertions = new ArrayList<>();

    public TestExecutor(CanaryTestDescriptor testDescriptor, InstanceContainer rootInstance, Point offset) {
        this.testDescriptor = testDescriptor;
        this.instance = rootInstance;
        this.origin = offset;
        Path structurePath = testDescriptor.getStructureLocation();
        Check.notNull(structurePath, "Missing structure for " + testDescriptor.getUniqueId());
        this.structure = structureIo.readStructure(structurePath);

        var tickListener = EventListener.builder(InstanceTickEvent.class)
                .handler(event -> this.tick(event.getDuration()))
                .filter(this::isValidTick).build();
        TICK_NODE.addListener(tickListener);

        createStructure();
    }

    @NotNull
    public CanaryTestDescriptor getTestDescriptor() {
        return testDescriptor;
    }

    @NotNull
    public Instance getInstance() {
        return instance;
    }

    public Structure getStructure() {
        return structure;
    }

    public Point getOrigin() {
        return origin;
    }

    public void register(AssertionImpl<?, ?> assertion) {
        assertions.add(assertion);
    }

    public void execute(TestExecutionListener listener) {
        if (running) {
            throw new IllegalStateException("Cannot execute a test while it is already running.");
        }

        // Update internal state
        executionCount++;
        executionListener = listener;

        // Instantiate class + execute test method & supporting.
        executionListener.start(testDescriptor);
        try {
            MethodSource source = (MethodSource) testDescriptor.getSource().get();
            classInstance = ReflectionUtils.newInstance(source.getJavaClass());
            var environment = new TestEnvironmentImpl(this);

            // "Before Each" methods
            for (Method method : testDescriptor.getPreEffects()) {
                invokeMethodOptionalParameter(method, classInstance, environment);
            }

            invokeMethodOptionalParameter(source.getJavaMethod(), classInstance, environment);

        } catch (Throwable throwable) {
            executionListener.end(testDescriptor, throwable);
        }

        running = true;
    }


    @Override
    public void tick(long time) {
        System.out.println("TICKING...");

        try {
            assertions.forEach(AssertionImpl::tick);
        } catch (AssertionError error) {
            end(error);
            return;
        }

        assertions.removeIf(AssertionImpl::hasDefinitiveResult);
        if (assertions.isEmpty()) {
            end(null);
        }

    }

    private boolean isValidTick(InstanceTickEvent event) {
        return this.running && this.instance.equals(event.getInstance());
    }

    public boolean isRunning() {
        return running;
    }

    private void end(@Nullable Throwable error) {
        System.out.println("ENDING TEST");
        // "After Each" methods
        var environment = new TestEnvironmentImpl(this); // We could keep track of the one from the init method, but it keeps no state so it doesnt really matter.
        for (Method method : testDescriptor.getPostEffects()) {
            invokeMethodOptionalParameter(method, classInstance, environment);
        }
        //todo reset stuff from test environment (like removing entities)

        // "Officially" end test
        executionListener.end(testDescriptor, error);

        // Reset state
        running = false;
        executionListener = null;
        classInstance = null;
        assertions.clear();

        // Reset structure
        //todo
    }

    private void createStructure() {
        // Visual Blocks
        var boundingBox = BoundingBoxHandler.BLOCK
                .withTag(BoundingBoxHandler.Tags.SizeX, structure.getSize().blockX())
                .withTag(BoundingBoxHandler.Tags.SizeY, structure.getSize().blockY())
                .withTag(BoundingBoxHandler.Tags.SizeZ, structure.getSize().blockZ());
        Point blockPos = origin.add(new Vec(0, -1, 0));

        getInstance().setBlock(blockPos, boundingBox);

        structure.loadIntoBlockSetter(getInstance(), origin);
        System.out.println("Loaded structure for " + getTestDescriptor().getUniqueId());
    }
}
