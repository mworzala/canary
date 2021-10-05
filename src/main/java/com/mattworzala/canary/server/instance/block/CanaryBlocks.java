package com.mattworzala.canary.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public interface CanaryBlocks {
    Block Beacon = BeaconHandler.BLOCK;

    static Block BoundingBox(@NotNull Point size) {
        return BoundingBoxHandler.BLOCK
                .withTag(BoundingBoxHandler.Tags.SizeX, size.blockX())
                .withTag(BoundingBoxHandler.Tags.SizeY, size.blockY())
                .withTag(BoundingBoxHandler.Tags.SizeZ, size.blockZ());
    }
}
