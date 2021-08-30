package com.mattworzala.canary.server.instance;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ChunkMirror extends Chunk {
    private final Chunk mirror;

    public ChunkMirror(@NotNull Instance instance, @NotNull Chunk mirror) {
        super(instance, mirror.getBiomes(), mirror.getChunkX(), mirror.getChunkZ(), mirror.shouldGenerate());

        this.mirror = mirror;
    }

    @Override
    public @NotNull Map<Integer, Section> getSections() {
        return mirror.getSections();
    }

    @Override
    public @NotNull Section getSection(int section) {
        return mirror.getSection(section);
    }

    @Override
    public void tick(long time) {
        mirror.tick(time);
    }

    @Override
    public long getLastChangeTime() {
        return mirror.getLastChangeTime();
    }

    @Override
    public void sendChunk(@NotNull Player player) {
        mirror.sendChunk(player);
    }

    @Override
    public void sendChunk() {
        mirror.sendChunk();
    }

    @Override
    public @NotNull Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
        return mirror.copy(instance, chunkX, chunkZ);
    }

    @Override
    public void reset() {
        mirror.reset();
    }

    @Override
    public @Nullable Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        return mirror.getBlock(x, y, z);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        mirror.setBlock(x, y, z, block);
    }
}
