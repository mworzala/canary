package com.mattworzala.canary.internal.server.instance;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasicGenerator implements ChunkGenerator {
    private final Block topBlock;

    public BasicGenerator() {
        this(Block.GRASS_BLOCK);
    }

    public BasicGenerator(@NotNull Block topBlock) {
        this.topBlock = topBlock;
    }

    @Override
    public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
        // Set chunk blocks
        for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (byte y = 0; y < 40; y++) {
                    batch.setBlock(x, y, z, Block.STONE);
                }
                batch.setBlock(x, 40, z, topBlock);
            }
        }
    }

    @Override
    public List<ChunkPopulator> getPopulators() {
        return null;
    }
}
