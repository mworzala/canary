package com.mattworzala.canary.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockSetter;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public interface CanaryBlocks {
    Block Beacon = BeaconHandler.BLOCK;

    static Block BoundingBox(@NotNull Point size) {
        return BoundingBoxHandler.BLOCK
                .withTag(BoundingBoxHandler.Tags.SizeX, size.blockX())
                .withTag(BoundingBoxHandler.Tags.SizeY, size.blockY())
                .withTag(BoundingBoxHandler.Tags.SizeZ, size.blockZ());
    }

    static Block Lectern(@NotNull String testName, @NotNull Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        return LecternHandler.BLOCK
                .withTag(LecternHandler.Tags.TestName, testName)
                .withTag(LecternHandler.Tags.TestFailure, error.getClass().getName() + ": " + error.getMessage())
                .withTag(LecternHandler.Tags.TestStacktrace, sw.toString());
    }


    static void placeBeacon(@NotNull BlockSetter target, @NotNull Point pos) {
        target.setBlock(pos, Beacon);
        target.setBlock(pos.sub(-1, 1, -1), Block.IRON_BLOCK);
        target.setBlock(pos.sub(0, 1, -1), Block.IRON_BLOCK);
        target.setBlock(pos.sub(1, 1, -1), Block.IRON_BLOCK);
        target.setBlock(pos.sub(-1, 1, 0), Block.IRON_BLOCK);
        target.setBlock(pos.sub(0, 1, 0), Block.IRON_BLOCK);
        target.setBlock(pos.sub(1, 1, 0), Block.IRON_BLOCK);
        target.setBlock(pos.sub(-1, 1, 1), Block.IRON_BLOCK);
        target.setBlock(pos.sub(0, 1, 1), Block.IRON_BLOCK);
        target.setBlock(pos.sub(1, 1, 1), Block.IRON_BLOCK);
    }
}
