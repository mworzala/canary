package com.mattworzala.canary.server;

import com.mattworzala.canary.server.block.StructureBlock;
import com.mattworzala.canary.server.command.StatusCommand;
import com.mattworzala.canary.server.command.TestsCommand;
import com.mattworzala.canary.server.instance.BasicGenerator;
import net.minestom.server.MinecraftServer;

import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.data.DataImpl;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.PlacementRules;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Position;

public class SandboxServer extends HeadlessServer {
    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        // Create the instance
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        // Set the ChunkGenerator
        instanceContainer.setChunkGenerator(new BasicGenerator());

        // Util options
        OptifineSupport.enable();
        PlacementRules.init();

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addEventCallback(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 41, 0));
        });
        globalEventHandler.addEventCallback(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) {
                event.getPlayer().setGameMode(GameMode.CREATIVE);
                event.getPlayer().setPermissionLevel(4);
            }
        });

        // Structure Block
//        MinecraftServer.getBlockManager().registerCustomBlock(new StructureBlock());
//        globalEventHandler.addEventCallback(PlayerBlockPlaceEvent.class, event -> {
//            if (event.getBlockStateId() != Block.STRUCTURE_BLOCK.getBlockId())
//                return;
//            event.setCustomBlock(StructureBlock.ID);
//            event.setBlockData(new DataImpl());
//        });

        registerCommands();

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }

    private static void registerCommands() {
        CommandManager commands = MinecraftServer.getCommandManager();
        commands.register(new StatusCommand());
        commands.register(new TestsCommand());
    }
}
