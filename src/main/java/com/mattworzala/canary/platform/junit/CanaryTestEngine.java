package com.mattworzala.canary.platform.junit;

import com.mattworzala.canary.platform.givemeahome.SandboxTestEnvironment;
import com.mattworzala.canary.platform.givemeahome.SandboxTestExecutor;
import com.mattworzala.canary.platform.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.platform.junit.discovery.CanaryDiscoverer;
import com.mattworzala.canary.platform.junit.execution.JUnitTestExecutionListenerAdapter;
import com.mattworzala.canary.platform.reflect.PHeadlessServer;
import com.mattworzala.canary.platform.util.MinestomMixin;
import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.*;

import java.util.Iterator;
import java.util.Optional;

import static org.junit.platform.engine.TestExecutionResult.successful;

@Environment(EnvType.PLATFORM)
public class CanaryTestEngine implements TestEngine {
    private static final Logger logger = LoggerFactory.getLogger(CanaryTestEngine.class);

    public static final String ID = "canary-test-engine";
    public static final String NAME = "Canary Test Engine";

    /**
     * If true, the engine is considered to be standalone. If standalone, the engine will:
     * - Initialize Mixin
     * - Start its own server
     * <p>
     * Otherwise mixin should be initialized on its own,
     * and {@link #setServer(PHeadlessServer)} should be called before executing any tests.
     */
    private final boolean standalone;
    private PHeadlessServer server;

    public CanaryTestEngine() {
        this(true);
    }

    public CanaryTestEngine(boolean initMixin) {
        this.standalone = initMixin;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of("com.mattworzala");
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of("canary");
    }

    @Override
    public Optional<String> getVersion() {
        return Optional.of("0.0.1");
    }

    public void setServer(@NotNull PHeadlessServer server) {
        this.server = server;
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        // We must initialize mixin before discovery, since it will load a large part of Minestom into the classloader on its own
        if (standalone) MinestomMixin.inject();
        return CanaryDiscoverer.discover(discoveryRequest, uniqueId);
    }

    @Override
    public void execute(ExecutionRequest request) {
        if (standalone)
            server = PHeadlessServer.create();
        Check.notNull(server, "Cannot execute tests without a server, please report this to the developers.");

        // Start headless server
        server.start();
        SandboxTestEnvironment.getInstance().setServer(server);

        // Execute all given tests
        EngineExecutionListener listener = request.getEngineExecutionListener();
        CanaryEngineDescriptor engineDescriptor = (CanaryEngineDescriptor) request.getRootTestDescriptor();
        listener.executionStarted(engineDescriptor);
        executeAllChildren(engineDescriptor, listener);
        listener.executionFinished(engineDescriptor, successful());

        // Stop headless server
        server.stop();
    }

    private void executeAllChildren(CanaryEngineDescriptor engineDescriptor, EngineExecutionListener listener) {
        var listenerAdapter = new JUnitTestExecutionListenerAdapter(listener);
        var executor = new SandboxTestExecutor(server, listenerAdapter);
        Iterator<TestDescriptor> iterator = engineDescriptor.getChildrenMutable().iterator();
        while (iterator.hasNext()) {
            executor.execute(iterator.next());
            iterator.remove();
        }
    }
}
