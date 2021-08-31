package com.mattworzala.canary.server.instance;

import com.mattworzala.canary.server.givemeahome.Structure;
import com.mattworzala.canary.server.givemeahome.TestExecutor;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ViewerInstance extends InstanceContainer {
    private static record MirrorConfig(Instance target, int realX, int realZ) { }
    private final Long2ObjectMap<MirrorConfig> testInstances = new Long2ObjectOpenHashMap<>();

    public ViewerInstance() {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);
        MinecraftServer.getInstanceManager().registerInstance(this);

        setChunkGenerator(new BasicGenerator());
        setChunkLoader(new Loader());
    }

    public void addMirror(Instance target, int fromX, int fromZ, int toX, int toZ) {
        testInstances.put(getChunkId(fromX, fromZ), new MirrorConfig(target, toX, toZ));
    }

    private long getChunkId(int x, int z) {
        return (((long) x) << 32) | z;
    }

    private class Loader implements IChunkLoader {

        @Override
        public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance viewerInstance, int chunkX, int chunkZ) {
            MirrorConfig config = testInstances.get(getChunkId(chunkX, chunkZ));
            if (config != null) {
                // Load a mirror of the chunk
                return config.target().loadChunk(config.realX(), config.realZ())
                        .thenApply(chunk -> {
                            if (!(chunk instanceof DynamicChunk dynChunk)) {
                                //todo just log error and return null
                                throw new RuntimeException("Cannot mirror any chunk implementation besides DynamicChunk");
                            }
                            return new ChunkMirror(viewerInstance, dynChunk, chunkX, chunkZ);
                        });
            } else {
                return CompletableFuture.completedFuture(null);
            }
        }

        @Override
        public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
