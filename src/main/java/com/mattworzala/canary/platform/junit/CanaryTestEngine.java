package com.mattworzala.canary.platform.junit;

import com.mattworzala.canary.platform.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.platform.junit.discovery.CanaryDiscoverer;
import com.mattworzala.canary.platform.junit.execution.JUnitTestExecutionListenerAdapter;
import com.mattworzala.canary.platform.reflect.ProxyHeadlessServer;
import com.mattworzala.canary.platform.reflect.ProxySandboxServer;
import com.mattworzala.canary.platform.reflect.ProxyTestCoordinator;
import com.mattworzala.canary.platform.util.MinestomMixin;
import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.*;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Environment(EnvType.PLATFORM)
public class CanaryTestEngine implements TestEngine {
    private static final Logger logger = LoggerFactory.getLogger(CanaryTestEngine.class);

    public static final String ID = "canary-test-engine";
    public static final String NAME = "Canary Test Engine";

    private final boolean isHeadless;
    private final ProxyHeadlessServer server;

    public CanaryTestEngine() {
        this(true);
    }

    public CanaryTestEngine(boolean isHeadless) {
        this.isHeadless = isHeadless;

        // Inject Mixin (must be done before creating the server, since it will load a large part of Minestom into the classloader on its own)
        MinestomMixin.inject();
        // Create server
        if (isHeadless) {
            this.server = ProxyHeadlessServer.create();
        } else {
            this.server = ProxySandboxServer.create();
        }
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

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        CanaryEngineDescriptor discoveryResult = CanaryDiscoverer.discover(discoveryRequest, uniqueId);

        ProxyTestCoordinator coordinator = server.getTestCoordinator();
        coordinator.indexTests(discoveryResult);

        return discoveryResult;
    }

    @Override
    public void execute(ExecutionRequest request) {
        // Start server on random port
        startServer();

        // Execute all with no filter
        ProxyTestCoordinator coordinator = server.getTestCoordinator();
        var junitListener = new JUnitTestExecutionListenerAdapter(request.getEngineExecutionListener());
        coordinator.execute(junitListener); // No filter set, will execute all tests
        // TestCoordinator#execute blocks until complete.

        stopServer();
    }

    public void startServer() {
        //todo is there a better way to choose a random port? (can the system choose for me somehow)
        int port = isHeadless ? ThreadLocalRandom.current().nextInt(30500, 30600) : 25565;
        startServer(port);
    }

    public void startServer(int port) {
        server.start(port);
    }

    public void stopServer() {
        server.stop();
    }


}
