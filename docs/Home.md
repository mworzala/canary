TODO

# Canary

## What is Canary?

Canary is an end-to-end testing framework for use with [Minestom](#what-is-minestom).

Since the main goal of Minestom is to provide high levels of customizabiilty to developers, people who make use of it
are expected to create write code for their desired behavior. We believe that this development process could be improved
by having the ability to create tests that can be run against their custom server.

For more information about how these tests work, checkout [here](Tests).

## What is Minestom?

Minestom is a java library that provides all the low level behavior of a minecraft server in the form of a library.
Minestom's main goal is to provide high levels of extensibility and highly performant execution.

An example of a simple usage of Minestom might look like this (taken from
their [wiki](https://wiki.minestom.net/setup/your-first-server)):

```java
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.world.biomes.Biome;

import java.util.Arrays;
import java.util.List;

public class MainDemo {

    public static void main(String[] args) {
        // Initialization of minetoms MinecraftServer
        MinecraftServer minecraftServer = MinecraftServer.init();
        // In order to make any instances we need to use the instance manager
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        // Create the instance
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        // Set the ChunkGenerator, to generate the blocks for the instance
        instanceContainer.setChunkGenerator(new GeneratorDemo());

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }

    // Minestom allows you to specify how to generate the blocks in the world
    // This is done using a chunk generator which will get called for every chunk in the world
    private static class GeneratorDemo implements ChunkGenerator {

        @Override
        public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
            // Set chunk blocks, this just creates a flat stone floor
            for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++)
                for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    for (byte y = 0; y < 40; y++) {
                        batch.setBlock(x, y, z, Block.STONE);
                    }
                }
        }

        @Override
        public void fillBiomes(Biome[] biomes, int chunkX, int chunkZ) {
            Arrays.fill(biomes, Biome.PLAINS);
        }

        @Override
        public List<ChunkPopulator> getPopulators() {
            return null;
        }
    }

}
```

Minestom itself provides essentially none of the "gameplay" features from vanilla Minecraft. Instead, Minestom allows
people to write their own custom behavior for these things. The use case for this is based on the fact that there are
many popular Minecraft servers that have heavily modified the default Minecraft server in order to provide custom
functionality. The goal of Minestom is to simplify and improve that customization process, allow unnecessary or unwanted
features to be omitted, and to be able to make use of a base server that is more performant than the vanilla Minecraft
version.

# Glossary

### Coordinate System

In minecraft the coordinate system specifies x and z as the two horizontal axes, and y as the vertical axis.

### Instance

[Minestom wiki entry](https://wiki.minestom.net/world/instances)

### Chunk

A chunk is a section of a world that is 16 blocks wide on the x and z axes. A chunk is 256 blocks tall along the y axis.

### World

### Structure

