package com.mattworzala.canary.junit.descriptor;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.util.Set;

public class CanaryEngineDescriptor extends EngineDescriptor {
    public CanaryEngineDescriptor(UniqueId uniqueId) {
        super(uniqueId, "Canary");
    }

    public Set<TestDescriptor> getChildrenMutable() {
        return children;
    }
}
