package com.mattworzala.canary.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ViewerInstance extends InstanceContainer {

    public ViewerInstance() {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);

        setChunkGenerator(new BasicGenerator());

        var viewerLoader = new ViewerChunkLoader();

        var testInstance = new TestInstance(Block.BLUE_STAINED_GLASS);
        MinecraftServer.getInstanceManager().registerInstance(testInstance);
        viewerLoader.testInstances.put((1L << 32) | 1, testInstance);

//        var testInstance2 = new TestInstance(Block.GREEN_STAINED_GLASS);
//        MinecraftServer.getInstanceManager().registerInstance(testInstance2);
//        viewerLoader.testInstances.put((2L << 32) | 1, testInstance2);

        setChunkLoader(viewerLoader);
    }

    @Override
    protected @NotNull CompletableFuture<@NotNull Chunk> retrieveChunk(int chunkX, int chunkZ) {
        return super.retrieveChunk(chunkX, chunkZ);
    }
}
