package com.mattworzala.canary.internal.server.sandbox.testbuilder;

import com.mattworzala.canary.internal.util.testbuilder.BlockBoundingBox;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockSetter;
import org.jetbrains.annotations.NotNull;

public class TestBuilderBlockSetter implements BlockSetter {
    private final Instance instance;
    private final BlockBoundingBox boundingBox;

    public TestBuilderBlockSetter(Instance instance, BlockBoundingBox boundingBox) {
        this.instance = instance;
        this.boundingBox = boundingBox;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        if (!block.isAir()) {
            boundingBox.addBlock(new Vec(x, y, z));
        }
        instance.setBlock(x, y, z, block);
    }
}
