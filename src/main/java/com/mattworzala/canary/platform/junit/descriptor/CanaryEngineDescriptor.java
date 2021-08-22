package com.mattworzala.canary.platform.junit.descriptor;

import com.mattworzala.canary.platform.junit.CanaryTestEngine;
import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.util.Collection;

@Environment(EnvType.GLOBAL)
public class CanaryEngineDescriptor extends EngineDescriptor {
    public CanaryEngineDescriptor(UniqueId uniqueId) {
        super(uniqueId, CanaryTestEngine.NAME);
    }

    @NotNull
    public Collection<TestDescriptor> getChildrenMutable() {
        return children;
    }
}
