package com.mattworzala.canary.internal.execution;

import com.mattworzala.canary.internal.assertion.AeSimpleParser;
import com.mattworzala.canary.internal.assertion.AssertionStep;
import com.mattworzala.canary.internal.assertion.Result;
import com.mattworzala.canary.internal.assertion.node.AeNode;
import com.mattworzala.canary.internal.execution.tracker.EntityTracker;
import com.mattworzala.canary.internal.execution.tracker.Tracker;
import com.mattworzala.canary.internal.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.internal.server.instance.block.CanaryBlocks;
import com.mattworzala.canary.internal.structure.JsonStructureIO;
import com.mattworzala.canary.internal.structure.Structure;
import com.mattworzala.canary.internal.util.ui.CameraPlayer;
import com.mattworzala.canary.internal.util.ui.MarkerUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.command.builder.arguments.ArgumentWord;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static com.mattworzala.canary.internal.util.ReflectionUtils.invokeMethodOptionalParameter;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private final List<Tracker<?>> trackers = List.of(new EntityTracker()); //todo: add more trackers or remove the system altogether
    private final Instance instance;
    private final Structure structure;
    private final Point origin;

    // Sandbox state
    //TODO: Factor this out of here. Could add some Minestom events which the executor triggers.
    private final Instance sandboxInstance;
    private final CameraPlayer camera;
    private final Point statusGlassBlock;
    private final Point failureLectern;

    // Mid-test state (anything which is accumulated while running a test)
    private volatile boolean running;
    private TestExecutionListener executionListener;
    private Object classInstance;
    private CompletableFuture<Void> task;
    private int lifetime;

    private final List<List<AssertionStep>> rawAssertions = new ArrayList<>();
    private final List<AeNode> assertions = new ArrayList<>();

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
        statusGlassBlock = origin.sub(0, 0, 1);
        failureLectern = origin.add(1, 0, -1);
        initialize();

        if (sandboxInstance != null) {
            camera = new CameraPlayer(this.instance, new Pos(origin).sub(new Pos(2, 0, 2)), new CopyOnWriteArrayList<>());
            MinecraftServer.getGlobalEventHandler().addListener(EventListener
                    .builder(PlayerLoginEvent.class)
                    .handler(event -> {
                        if (event.getPlayer() instanceof CameraPlayer)
                            return;
                        camera.addCameraViewer(event.getPlayer());
                    })
                    .build());
        } else camera = null;
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

    public List<AssertionStep> createEmptyAssertion() {
        List<AssertionStep> assertionSteps = new ArrayList<>();
        rawAssertions.add(assertionSteps);
        return assertionSteps;
    }

    public <T> void track(T object) {
        //noinspection unchecked
        Tracker<T> tracker = (Tracker<T>) trackers.stream().filter(t -> t.canTrack(object)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No valid tracker for: " + object.toString()));
        tracker.track(object);
    }

    public void execute(TestExecutionListener listener, CompletableFuture<Void> task) {
        if (running) {
            throw new IllegalStateException("Cannot execute a test while it is already running.");
        }

        // Update internal state
        trackers.forEach(Tracker::release);
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

            // Invoke the actual test method
            TestEnvironmentImpl.CURRENT.set(environment);
            invokeMethodOptionalParameter(source.getJavaMethod(), classInstance, environment);
            TestEnvironmentImpl.CURRENT.set(null);

            // Compile assertions
            for (var assertionSteps : rawAssertions) {
                AeNode node = new AeSimpleParser(assertionSteps).parse();
                if (node == null) {
                    throw new RuntimeException("Failed to compile assertion!"); //todo can show errors here, but probably want stacktrace elements to render
                }
                assertions.add(node);
            }
            rawAssertions.clear();
        } catch (Throwable throwable) {
            end(throwable);
            return;
        }

        this.task = task;
        lifetime = 100;
        running = true;
    }

    @Override
    public void tick(long time) {

        try {
            boolean anyFail = false;
            var iter = assertions.iterator();
            while (iter.hasNext()) {
                Result result = iter.next().evaluate(null);

                if (result.isPass()) {
                    iter.remove();
                } else if (result.isFail()) {
                    anyFail = true;
                }
            }

            if (!anyFail) {
                // The only remaining tests are soft passes, so we can pass
                end(null);
            }
        } catch (Throwable throwable) {
            // An unknown error has occurred while evaluating the assertions
            end(new AssertionError("An unknown error occurred while evaluating an assertion.", throwable));
            return;
        }

        if (--lifetime < 0) {
            // TEMP add the markers from the first fail
            ((Result.FailResult) assertions.get(0).evaluate(null)).getMarkers().forEach(marker -> {
                MarkerUtil.Marker adjusted = new MarkerUtil.Marker(marker.position().add(getOrigin()), marker.color(), marker.message());
                MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> MarkerUtil.sendTestMarker(player, adjusted));
            });

            //todo print the failing tests
            end(new RuntimeException("Test timed out."));
        }
    }

    private boolean isValidTick(InstanceTickEvent event) {
        return this.running && this.instance.equals(event.getInstance());
    }

    public boolean isRunning() {
        return running;
    }

    private void end(@Nullable Throwable error) {

        // "Officially" end test
        executionListener.end(testDescriptor, error);
        setVisualStatus(error == null);
        if (error != null && sandboxInstance != null) {
            sandboxInstance.setBlock(failureLectern, CanaryBlocks.Lectern(getTestDescriptor().getDisplayName(), error));
        }

        // "After Each" methods
        var environment = new TestEnvironmentImpl(this); // We could keep track of the one from the init method, but it keeps no state so it doesnt really matter.
        for (Method method : testDescriptor.getPostEffects()) {
            invokeMethodOptionalParameter(method, classInstance, environment);
        }

        // Reset state
        if (error == null) {
            // Leave tracked items if this was a failure, they will be removed if you try to run the test again anyway.
            trackers.forEach(Tracker::release);
        }
        running = false;
        executionListener = null;
        classInstance = null;
        assertions.clear();

        // Reset structure
        structure.loadIntoBlockSetter(instance, origin);
        if (sandboxInstance != null) structure.loadIntoBlockSetter(sandboxInstance, origin);

        task.complete(null);
        task = null;
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
        double minBlockX = origin.blockX() - LOAD_AREA, maxBlockX = origin.blockX() + structure.getSizeX() + LOAD_AREA;
        double minBlockZ = origin.blockZ() - LOAD_AREA, maxBlockZ = origin.blockZ() + structure.getSizeZ() + LOAD_AREA;

        // Load relevant chunks in parallel
        List<CompletableFuture<?>> loadRequests = new ArrayList<>();
        for (double x = Math.floor(minBlockX / Chunk.CHUNK_SIZE_X); x <= maxBlockX / Chunk.CHUNK_SIZE_X; x++) {
            for (double z = minBlockZ / Chunk.CHUNK_SIZE_Z; z <= maxBlockZ / Chunk.CHUNK_SIZE_Z; z++) {
                loadRequests.add(instance.loadChunk((int) x, (int) z));
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
