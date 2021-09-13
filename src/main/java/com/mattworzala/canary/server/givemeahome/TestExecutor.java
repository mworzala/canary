package com.mattworzala.canary.server.givemeahome;

import com.mattworzala.canary.api.TestEnvironment;
import com.mattworzala.canary.platform.givemeahome.TestExecutionListener;
import com.mattworzala.canary.platform.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.server.assertion.AssertionImpl;
import com.mattworzala.canary.server.env.TestEnvironmentImpl;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// one per test method (reused), handles instantiating the test class, invoking the before/run/after methods, cleaning up the test for the next execution (replace structure).
//   Executing a test is not blocking, it must be ticked until it reports that it has a result.
public class TestExecutor implements Tickable {
    private static final EventFilter<InstanceTickEvent, Instance> FILTER_INSTANCE_TICK = EventFilter.from(InstanceTickEvent.class, Instance.class, InstanceTickEvent::getInstance);
    private static final EventNode<InstanceTickEvent> TICK_NODE = EventNode.type("EventExecutor_InstanceTick", FILTER_INSTANCE_TICK);
    static { MinecraftServer.getGlobalEventHandler().addChild(TICK_NODE); }

    private final CanaryTestDescriptor testDescriptor;

    private int executionCount = 0;
    private final TestInstance instance;
    private final Structure structure;
    // The position in the viewer instance. Only used if we are running in sandbox mode.
//    private final Point origin; //todo

    // Mid-test state (anything which is accumulated while running a test)
    private volatile boolean running;
    private TestExecutionListener executionListener;
    private final List<AssertionImpl<?, ?>> assertions = new ArrayList<>();

    public TestExecutor(CanaryTestDescriptor testDescriptor) {
        this.testDescriptor = testDescriptor;
        this.instance = new TestInstance();
        this.structure = new Structure(new Vec(15, 15, 15));

        var tickListener = EventListener.builder(InstanceTickEvent.class)
                .handler(event -> this.tick(event.getDuration()))
                .filter(this::isValidTick).build();
        TICK_NODE.addListener(tickListener);

//        createStructure();
    }

    @NotNull
    public CanaryTestDescriptor getTestDescriptor() {
        return testDescriptor;
    }

    @NotNull
    public TestInstance getInstance() {
        return instance;
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
            Object classInstance = createEnclosingClass(source);
            var environment = new TestEnvironmentImpl(this);

            //todo beforeEach/All

            invokeTestMethod(source.getJavaMethod(), classInstance, environment);

            //todo afterEach/All

        } catch (Throwable throwable) {
            executionListener.end(testDescriptor, throwable);
        }

        running = true;
    }

    @Override
    public void tick(long time) {
        System.out.println("TICKING...");

        executionListener.end(testDescriptor, new RuntimeException("AN ERROR"));
        reset();
    }

    public void reset() {
        // Reset state
        running = false;
        executionListener = null;
        assertions.clear();

        // Reset structure
        //todo
    }

    private boolean isValidTick(InstanceTickEvent event) {
        return this.running && this.instance.equals(event.getInstance());
    }

    private void createStructure() {
        // Visual Blocks
        instance.setBlock(0, 41, 0, CanaryBlocks.BoundingBox(structure.size()));

//        instance.setBlock(0, 42, 0, Block.LECTERN);

        //todo place structure
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Invokes any method related to a test which takes parameters such as the environment
     */
    private void invokeTestMethod(Method method, Object instance, TestEnvironment env) {
        if (method.getParameterCount() == 1) {
            ReflectionUtils.invokeMethod(method, instance, env);
        } else {
            ReflectionUtils.invokeMethod(method, instance);
        }
    }

    private Object createEnclosingClass(MethodSource source) {
        Class<?> target = source.getJavaClass();
        return ReflectionUtils.newInstance(target);
    }
}
