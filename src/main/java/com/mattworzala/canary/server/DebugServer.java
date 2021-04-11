package com.mattworzala.canary.server;

import com.mattworzala.canary.server.block.StructureBlock;
import com.mattworzala.canary.server.world.NoiseGenerator;
import net.minestom.server.MinecraftServer;

import net.minestom.server.data.DataImpl;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.extras.PlacementRules;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;
import org.jglrxavpok.hephaistos.nbt.*;

public class DebugServer extends HeadlessServer {
    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        // Create the instance
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        // Set the ChunkGenerator
        instanceContainer.setChunkGenerator(new NoiseGenerator());

        // Util options
        OptifineSupport.enable();
        PlacementRules.init();

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addEventCallback(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Position(0, 100, 0));
            player.setPermissionLevel(4);
        });
        globalEventHandler.addEventCallback(PlayerSpawnEvent.class, event -> {
            event.getPlayer().getInventory().addItemStack(new ItemStack(Material.STRUCTURE_BLOCK));
            event.getPlayer().setGameMode(GameMode.CREATIVE);
            event.getPlayer().setPermissionLevel(4);
        });

        globalEventHandler.addEventCallback(PlayerUseItemOnBlockEvent.class, event -> {
            Player p = event.getPlayer();
            if(event.getItemStack().getMaterial() == Material.HORSE_SPAWN_EGG) {
                Position spawnPosition = new Position(event.getPosition().getX(), event.getPosition().getY(), event.getPosition().getZ());
                Entity horse = new Entity(EntityType.HORSE);

                horse.setInstance(p.getInstance(), event.getPosition().toPosition().add(0, 1, 0));
            }
        });

        // Structure Block
        MinecraftServer.getBlockManager().registerCustomBlock(new StructureBlock());
        globalEventHandler.addEventCallback(PlayerBlockPlaceEvent.class, event -> {
            if (event.getBlockStateId() != Block.STRUCTURE_BLOCK.getBlockId())
                return;
            event.setCustomBlock(StructureBlock.ID);
            event.setBlockData(new DataImpl());
        });


//        globalEventHandler.addEventCallback(PlayerBlockPlaceEvent.class, event -> {
//            MinecraftServer.getSchedulerManager().buildTask(() -> {
//                if (event.getBlockStateId() == Block.STRUCTURE_BLOCK.getBlockId()) {
//
//
//                    BlockEntityDataPacket bEDP = new BlockEntityDataPacket();
//                    bEDP.blockPosition = event.getBlockPosition();
//                    bEDP.action = 7;
//                    NBTCompound nbt = new NBTCompound();
//                    nbt.set("author", new NBTString("JimtheJamMan"));
//                    nbt.set("ignoreEntities", new NBTByte((byte) 1));
//                    nbt.set("integrity", new NBTFloat(1.0f));
//                    nbt.set("mirror", new NBTString("NONE"));
//                    nbt.set("metadata", new NBTString(""));
//                    nbt.set("mode", new NBTString("DATA"));
//                    nbt.set("name", new NBTString(""));
//                    nbt.set("posX", new NBTInt(0));
//                    nbt.set("posY", new NBTInt(1));
//                    nbt.set("posZ", new NBTInt(0));
//                    nbt.set("x", new NBTInt(event.getBlockPosition().getX()));
//                    nbt.set("y", new NBTInt(event.getBlockPosition().getY()));
//                    nbt.set("z", new NBTInt(event.getBlockPosition().getZ()));
//                    nbt.set("id", new NBTString("minecraft:structure_block"));
//                    nbt.set("powered", new NBTByte((byte) 0));
//                    nbt.set("showair", new NBTByte((byte) 0));
//                    nbt.set("rotation", new NBTString("NONE"));
//                    nbt.set("seed", new NBTLong(0));
//                    nbt.set("showboundingbox", new NBTByte((byte) 1));
//                    nbt.set("sizeX", new NBTInt(0));
//                    nbt.set("sizeY", new NBTInt(0));
//                    nbt.set("sizeZ", new NBTInt(0));
//                    bEDP.nbtCompound = nbt;
//                    event.getPlayer().getPlayerConnection().sendPacket(bEDP);
//
////                    BlockChangePacket blockChangePacket = new BlockChangePacket();
////                    blockChangePacket.blockPosition = event.getBlockPosition();
////                    blockChangePacket.blockStateId = 15743;
////                    event.getPlayer().getPlayerConnection().sendPacket(blockChangePacket);
//
//                    bEDP = new BlockEntityDataPacket();
//                    bEDP.blockPosition = event.getBlockPosition();
//                    bEDP.action = 7;
//                    nbt = new NBTCompound();
//                    nbt.set("author", new NBTString("JimtheJamMan"));
//                    nbt.set("ignoreEntities", new NBTByte((byte) 1));
//                    nbt.set("integrity", new NBTFloat(1.0f));
//                    nbt.set("mirror", new NBTString("NONE"));
//                    nbt.set("metadata", new NBTString(""));
//                    nbt.set("mode", new NBTString("SAVE"));
//                    nbt.set("name", new NBTString(""));
//                    nbt.set("posX", new NBTInt(0));
//                    nbt.set("posY", new NBTInt(1));
//                    nbt.set("posZ", new NBTInt(0));
//                    nbt.set("x", new NBTInt(event.getBlockPosition().getX()));
//                    nbt.set("y", new NBTInt(event.getBlockPosition().getY()));
//                    nbt.set("z", new NBTInt(event.getBlockPosition().getZ()));
//                    nbt.set("id", new NBTString("minecraft:structure_block"));
//                    nbt.set("powered", new NBTByte((byte) 0));
//                    nbt.set("showair", new NBTByte((byte) 0));
//                    nbt.set("rotation", new NBTString("NONE"));
//                    nbt.set("seed", new NBTLong(0));
//                    nbt.set("showboundingbox", new NBTByte((byte) 1));
//                    nbt.set("sizeX", new NBTInt(1));
//                    nbt.set("sizeY", new NBTInt(1));
//                    nbt.set("sizeZ", new NBTInt(1));
//                    bEDP.nbtCompound = nbt;
//                    event.getPlayer().getPlayerConnection().sendPacket(bEDP);
//                }
//            }).delay(0, TimeUnit.TICK).schedule();
//        });

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }
}
