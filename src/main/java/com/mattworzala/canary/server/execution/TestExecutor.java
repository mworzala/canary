package com.mattworzala.canary.server.execution;

import com.mattworzala.canary.platform.TestExecutionListener;
import com.mattworzala.canary.platform.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.server.assertion.AssertionImpl;
import com.mattworzala.canary.server.env.TestEnvironmentImpl;
import com.mattworzala.canary.server.instance.block.CanaryBlocks;
import com.mattworzala.canary.server.structure.JsonStructureIO;
import com.mattworzala.canary.server.structure.Structure;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.mattworzala.canary.platform.util.ReflectionUtils.invokeMethodOptionalParameter;

// one per test method (reused), handles instantiating the test class, invoking the before/run/after methods, cleaning up the test for the next execution (replace structure).
//   Executing a test is not blocking, it must be ticked until it reports that it has a result.
public class TestExecutor implements Tickable {
    /**
     * The extra area around the structure to be loaded.
     */
    private static final int LOAD_AREA = 16;

    private static final EventFilter<InstanceTickEvent, Instance> FILTER_INSTANCE_TICK = EventFilter.from(InstanceTickEvent.class, Instance.class, InstanceTickEvent::getInstance);
    private static final EventNode<InstanceTickEvent> TICK_NODE = EventNode.type("EventExecutor_InstanceTick", FILTER_INSTANCE_TICK);

    private static final JsonStructureIO structureIo = new JsonStructureIO();

    static {
        MinecraftServer.getGlobalEventHandler().addChild(TICK_NODE);
    }

    private final CanaryTestDescriptor testDescriptor;

    private final Instance instance;
    private final Structure structure;
    private final Point origin;

    // Sandbox state
    private final Instance sandboxInstance;
    private final CameraPlayer camera;
    private final Point statusGlassBlock;
    private final Point failureLectern;

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

        // Start ticking
        var tickListener = EventListener.builder(InstanceTickEvent.class)
                .handler(event -> this.tick(event.getDuration()))
                .filter(this::isValidTick).build();
        TICK_NODE.addListener(tickListener);

        sandboxInstance = MinecraftServer.getInstanceManager().getInstance(new UUID(0, 0));
        camera = new CameraPlayer(this.instance, new Pos(origin).sub(new Pos(2, 0, 2)), new CopyOnWriteArrayList<>());
        statusGlassBlock = origin.sub(0, 0, 1);
        failureLectern = origin.add(1, 0, -1);
        initialize();

        MinecraftServer.getGlobalEventHandler().addListener(EventListener
                .builder(PlayerLoginEvent.class)
                .handler(event -> {
                    if (event.getPlayer() instanceof CameraPlayer)
                        return;
                    camera.addCameraViewer(event.getPlayer());
                })
                .build());
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
        executionListener = listener;

        // Instantiate class + execute test method & supporting.
        executionListener.start(testDescriptor);
        setVisualStatus(null);
        if (sandboxInstance != null) {
            sandboxInstance.setBlock(failureLectern, Block.AIR);
        }

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
            end(throwable);
            return;
        }

        running = true;
    }


    @Override
    public void tick(long time) {

        throw new RuntimeException("Test ticking not implemented.");

//        try {
//            assertions.forEach(AssertionImpl::tick); //todo
//        } catch (AssertionError error) {
//            end(error);
//            return;
//        }
//
//        assertions.removeIf(AssertionImpl::hasDefinitiveResult);
//        if (assertions.isEmpty()) {
//            end(null);
//        }

    }

    private boolean isValidTick(InstanceTickEvent event) {
        return this.running && this.instance.equals(event.getInstance());
    }

    public boolean isRunning() {
        return running;
    }

    private void end(@Nullable Throwable error) {

        // "After Each" methods
        var environment = new TestEnvironmentImpl(this); // We could keep track of the one from the init method, but it keeps no state so it doesnt really matter.
        for (Method method : testDescriptor.getPostEffects()) {
            invokeMethodOptionalParameter(method, classInstance, environment);
        }
        //todo reset stuff from test environment (like removing entities)

        // "Officially" end test
        executionListener.end(testDescriptor, error);
        setVisualStatus(error == null);
        if (error != null && sandboxInstance != null) {
            sandboxInstance.setBlock(failureLectern, CanaryBlocks.Lectern(getTestDescriptor().getDisplayName(), error));
        }

        // Reset state
        running = false;
        executionListener = null;
        classInstance = null;
        assertions.clear();

        // Reset structure
        structure.loadIntoBlockSetter(instance, origin);
        if (sandboxInstance != null) structure.loadIntoBlockSetter(sandboxInstance, origin);
    }

    private void initialize() {
        loadWorldRegion(instance);
        if (sandboxInstance != null) loadWorldRegion(sandboxInstance);

        structure.loadIntoBlockSetter(instance, origin);
        if (sandboxInstance != null) {
            structure.loadIntoBlockSetter(sandboxInstance, origin);

            // Bounding box
            var boundingBox = CanaryBlocks.BoundingBox(structure.getSize());
            sandboxInstance.setBlock(origin.sub(0, 1, 0), boundingBox);

            // Beacon
            CanaryBlocks.placeBeacon(sandboxInstance, origin.sub(0, 1, 1));
            setVisualStatus(null);
        }
    }

    private void loadWorldRegion(Instance instance) {
        int minBlockX = origin.blockX() - LOAD_AREA, maxBlockX = origin.blockX() + structure.getSizeX() + LOAD_AREA;
        int minBlockZ = origin.blockZ() - LOAD_AREA, maxBlockZ = origin.blockZ() + structure.getSizeZ() + LOAD_AREA;

        // Load relevant chunks in parallel
        List<CompletableFuture<?>> loadRequests = new ArrayList<>();
        for (int x = minBlockX / Chunk.CHUNK_SIZE_X; x <= maxBlockX / Chunk.CHUNK_SIZE_X; x++) {
            for (int z = minBlockZ / Chunk.CHUNK_SIZE_Z; z <= maxBlockZ / Chunk.CHUNK_SIZE_Z; z++) {
                loadRequests.add(instance.loadChunk(x, z));
            }
        }

        // Wait for all loads to complete
        CompletableFuture.allOf(loadRequests.toArray(new CompletableFuture<?>[0])).join();
    }

    private void setVisualStatus(@Nullable Boolean status) {
        if (sandboxInstance == null) return;

        if (status == null) {
            sandboxInstance.setBlock(statusGlassBlock, Block.LIGHT_GRAY_STAINED_GLASS);
        } else if (status) {
            sandboxInstance.setBlock(statusGlassBlock, Block.GREEN_STAINED_GLASS);
        } else {
            sandboxInstance.setBlock(statusGlassBlock, Block.RED_STAINED_GLASS);
        }
    }
}
