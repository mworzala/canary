package com.mattworzala.canary.server.instance;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ViewerChunkLoader implements IChunkLoader {
    public final Long2ObjectMap<TestInstance> testInstances = new Long2ObjectOpenHashMap<>();

    @Override
    public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance viewerInstance, int chunkX, int chunkZ) {
        var cf = new CompletableFuture<@Nullable Chunk>();

        long id = (((long) chunkX) << 32) | chunkZ;
        TestInstance instance = testInstances.get(id);
        if (instance != null) {
            //todo we do not want to return null if the test instance doesnt have this chunk loaded
            var chunk = instance.loadChunk(chunkX, chunkZ).join();
            cf.complete(chunk);
            System.out.println("Loaded (" + chunkX + ", " + chunkZ + ") from " + instance);
        } else {
            cf.complete(null);
        }

        return cf;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        var cf = new CompletableFuture<Void>();
        cf.complete(null);
        return cf;
    }
}
