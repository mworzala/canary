package com.mattworzala.canary.internal.server.instance;

import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VoidGenerator implements ChunkGenerator {

    @Override
    public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
        // Add nothing
    }

    @Override
    public List<ChunkPopulator> getPopulators() {
        return null;
    }
}
