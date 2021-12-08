package com.mattworzala.canary.internal.launch;

import com.mattworzala.canary.internal.junit.CanaryTestEngine;
import com.mattworzala.canary.internal.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.internal.util.reflect.ProxyTestCoordinator;
import com.mattworzala.canary.internal.util.safety.Env;
import com.mattworzala.canary.internal.util.safety.EnvType;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

@Env(EnvType.PLATFORM)
public class SandboxLauncher {
    /**
     * A JUnit engine discovery request which allows any class in any package.
     */
    private static final LauncherDiscoveryRequest DEFAULT_REQUEST = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectPackage(""))
            .build();

    public static void main(String[] args) {
        System.setProperty("minestom.viewable-packet", "false");



        var engine = new CanaryTestEngine(false);

        // Discover tests
        CanaryEngineDescriptor discoveryResult = engine.discover(DEFAULT_REQUEST, UniqueId.forEngine(engine.getId()));

        // Start the server manually since we are not calling `engine#execute(..)`
        engine.init();
        engine.startServer();

        // Inform the test coordinator of the discovered tests
        ProxyTestCoordinator testCoordinator = engine.getServer().getTestCoordinator();
        testCoordinator.indexTests(discoveryResult);

        // Stopped by other means
    }
}
