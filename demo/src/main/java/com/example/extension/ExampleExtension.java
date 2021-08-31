package com.example.extension;

import com.example.extension.command.EntityCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.utils.binary.BinaryWriter;

import java.awt.*;

public class ExampleExtension extends Extension {
    public static long blockPosToLong(int x, int y, int z) {
        return ((long) x & 67108863L) << 38 | (long) y & 4095L | ((long) z & 67108863L) << 12;
    }

    @Override
    public void initialize() {
        System.out.println("Hello from example!");

        registerCommands();

        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockInteractEvent.class, this::onPlayerInteract);

        // Send debug/neighbors_update right here (if some debug mode is enabled): https://github.com/Minestom/Minestom/blob/new-block-api/src/main/java/net/minestom/server/instance/DynamicChunk.java#L93


        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            final var player = event.getPlayer();
            final var pos = new Vec(0, 40, 0);
            player.setPermissionLevel(4);

            var frequency = .3;
            for (var i = 0; i < 12; ++i) {
                int red = (int) (Math.sin(frequency * i + 0) * 127 + 128);
                int green = (int) (Math.sin(frequency * i + 2) * 127 + 128);
                int blue = (int) (Math.sin(frequency * i + 4) * 127 + 128);


                BinaryWriter writer = new BinaryWriter();
                writer.writeLong(blockPosToLong(pos.add(i * 2, 0, 0).blockX(), pos.blockY(), pos.blockZ()));
                System.out.printf("%d %d %d %d\n", red, green, blue, 255);
                writer.writeInt(new Color(red, green, blue).getRGB());
                writer.writeSizedString("test");
                writer.writeInt(100000);

                player.sendPluginMessage("minecraft:debug/game_test_add_marker", writer.toByteArray());
            }

        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSwapItemEvent.class, event -> {
            System.out.println("Player swap item event");
            System.out.println(event);
        });
    }

    private void onPlayerInteract(PlayerBlockInteractEvent event) {
        try {
            final var player = event.getPlayer();
            final var pos = event.getBlockPosition();

            BinaryWriter writer = new BinaryWriter();
////            writer.writeBlockPosition(event.getBlockPosition());
//            writer.writeLong(blockPosToLong(pos.blockX(), pos.blockY(), pos.blockZ()));
//            writer.writeInt(new Color(255, 255, 255, 128).getRGB());
//            writer.writeSizedString("test123");
//            writer.writeInt(5000);
//            player.sendPluginMessage("minecraft:debug/game_test_add_marker", writer.toByteArray());

            writer.writeVarLong(System.currentTimeMillis());
            writer.writeLong(blockPosToLong(pos.blockX(), pos.blockY(), pos.blockZ()));
            player.sendPluginMessage("minecraft:debug/neighbors_update", writer.toByteArray());

            System.out.println("SENDING DEBUG THING");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void terminate() {

    }

    private void registerCommands() {
        var commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new EntityCommand());
    }
}
