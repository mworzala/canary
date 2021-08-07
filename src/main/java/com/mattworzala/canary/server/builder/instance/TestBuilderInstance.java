package com.mattworzala.canary.server.builder.instance;

import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TestBuilderInstance extends InstanceContainer {
    public static final DimensionType DIMENSION_TYPE = DimensionType.builder(NamespaceID.from("canary:test_builder"))
            .ultrawarm(false)
            .natural(true)
            .piglinSafe(false)
            .respawnAnchorSafe(false)
            .bedSafe(true)
            .raidCapable(true)
            .skylightEnabled(true)
            .ceilingEnabled(false)
            .fixedTime(null)
            .ambientLight(0.0f)
            .logicalHeight(256)
            .infiniburn(NamespaceID.from("minecraft:infiniburn_overworld"))
            .build(); //todo height, light, etc

    public TestBuilderInstance(@NotNull UUID uniqueId) {
        super(uniqueId, DIMENSION_TYPE);
    }
}
