package com.mattworzala.canary.server.instance;

import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;

import java.util.UUID;

public class TestInstance extends InstanceContainer {

    public TestInstance(Block block) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD /* todo */);

        setChunkGenerator(new BasicGenerator(block));
    }
}
