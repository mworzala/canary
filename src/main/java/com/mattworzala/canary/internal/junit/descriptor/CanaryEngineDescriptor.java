package com.mattworzala.canary.internal.junit.descriptor;

import com.mattworzala.canary.internal.util.safety.EnvType;
import com.mattworzala.canary.internal.util.safety.Env;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.util.Collection;

@Env(EnvType.GLOBAL)
public class CanaryEngineDescriptor extends EngineDescriptor {
    public static final String NAME = "Canary Test Engine";

    public CanaryEngineDescriptor(UniqueId uniqueId) {
        super(uniqueId, NAME);
    }

    @NotNull
    public Collection<TestDescriptor> getChildrenMutable() {
        return children;
    }
}
