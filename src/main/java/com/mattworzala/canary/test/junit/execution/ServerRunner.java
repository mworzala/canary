package com.mattworzala.canary.test.junit.execution;

import com.mattworzala.canary.test.junit.descriptor.CanaryEngineDescriptor;
import net.minestom.server.Bootstrap;

public class ServerRunner {
    private final CanaryEngineDescriptor descriptor;

    public ServerRunner(CanaryEngineDescriptor descriptor) {
        this.descriptor = descriptor;

    }
}
