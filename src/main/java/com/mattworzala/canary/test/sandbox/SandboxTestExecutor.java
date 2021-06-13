package com.mattworzala.canary.test.sandbox;

import com.mattworzala.canary.test.junit.CanaryTestEngine;
import com.mattworzala.canary.test.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.test.junit.descriptor.CanaryTestDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

public class SandboxTestExecutor {
    private static CanaryEngineDescriptor root;

    @NotNull
    public static CanaryEngineDescriptor getRoot() {
        return root;
    }

    public static void init() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage(""))
                .build();
        CanaryTestEngine engine = new CanaryTestEngine();
        root = (CanaryEngineDescriptor) engine.discover(request, UniqueId.forEngine(engine.getId()));
    }
}
