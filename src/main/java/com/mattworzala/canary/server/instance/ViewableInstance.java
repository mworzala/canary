package com.mattworzala.canary.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkMirror;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ViewableInstance extends InstanceContainer {

    public ViewableInstance(Block block) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD /* todo */);
        MinecraftServer.getInstanceManager().registerInstance(this);

        setChunkGenerator(new BasicGenerator(block));
    }

    @Override
    public synchronized void UNSAFE_switchEntityChunk(@NotNull Entity entity, @NotNull Chunk lastChunk, @NotNull Chunk newChunk) {
        super.UNSAFE_switchEntityChunk(entity, lastChunk, newChunk);
        if (newChunk.getChunkX() == 1 && newChunk.getChunkZ() == 1) {
            final Chunk viewerChunk = MinecraftServer.getInstanceManager().getInstance(ViewerInstance.ID).getChunk(1, 1);
            for (Player viewer : viewerChunk.getViewers()) {
                entity.addViewer(viewer);
            }
        }
        if (lastChunk.getChunkX() == 1 && lastChunk.getChunkZ() == 1) {
            final Chunk viewerChunk = MinecraftServer.getInstanceManager().getInstance(ViewerInstance.ID).getChunk(1, 1);
            for (Player viewer : viewerChunk.getViewers()) {
                entity.removeViewer(viewer);
            }
        }
    }
}
