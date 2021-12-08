package com.example.extension;

import com.example.extension.command.EntityCommand;
import com.example.extension.minecart.RailPlacementRule;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.binary.BinaryWriter;

import java.awt.*;

public class ExampleExtension extends Extension {
    @Override
    public void initialize() {
        System.out.println("Hello from example!");

        registerCommands();
        registerPlacementRules();

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            final var player = event.getPlayer();

            if (event.isFirstSpawn()) {
                player.getInventory().addItemStack(ItemStack.of(Material.RAIL));
            }
        });
    }

    @Override
    public void terminate() {

    }

    private void registerCommands() {
        var commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new EntityCommand());
    }

    private void registerPlacementRules() {
        var blockManager = MinecraftServer.getBlockManager();
        blockManager.registerBlockPlacementRule(new RailPlacementRule());
    }
}
