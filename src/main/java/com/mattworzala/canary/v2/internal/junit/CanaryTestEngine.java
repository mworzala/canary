package com.mattworzala.canary.v2.internal.junit;

import org.junit.jupiter.engine.descriptor.JupiterTestDescriptor;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;

public class CanaryTestEngine extends HierarchicalTestEngine<CanaryEngineExecutionContext> {

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        AbstractTestDescriptor
        return null;
    }

    @Override
    protected CanaryEngineExecutionContext createExecutionContext(ExecutionRequest request) {
        return null;
    }

    @Override
    public String getId() {
        return "canary";
    }
}
