package com.mattworzala.canary.server;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.command.*;
import com.mattworzala.canary.server.instance.BasicGenerator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.PlacementRules;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.resourcepack.ResourcePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Environment(EnvType.MINESTOM)
public class SandboxServer extends HeadlessServer {
    private static final Logger logger = LoggerFactory.getLogger(SandboxServer.class);

    private static final String RESOURCE_PACK_URL = "https://raw.githubusercontent.com/mworzala/canary/resourcepack/canary_helper.zip";
    private static final String RESOURCE_PACK_HASH_URL = "https://raw.githubusercontent.com/mworzala/canary/resourcepack/canary_helper.sha1";
    private static final String RESOURCE_PACK_HASH;
    static {
        String hash = "";
        try (InputStream in = new URL(RESOURCE_PACK_HASH_URL).openStream()) {
            hash = new String(in.readAllBytes()).substring(0, 40);
        } catch (IOException ignored) {
            logger.warn("Unable to read resource pack hash!");
        }
        RESOURCE_PACK_HASH = hash;
    }

    @Override
    public void initServer() {
        super.initServer();
        headless = true;

        // Util options
        OptifineSupport.enable();
        PlacementRules.init();
        MojangAuth.init();

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addEventCallback(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            var instance = MinecraftServer.getInstanceManager().createInstanceContainer();
            instance.setChunkGenerator(new BasicGenerator());
            event.setSpawningInstance(instance);
            player.setRespawnPoint(new Pos(0, 41, 0));

        });
        globalEventHandler.addEventCallback(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) {
                event.getPlayer().setGameMode(GameMode.CREATIVE);
                event.getPlayer().setPermissionLevel(4);

                if (!RESOURCE_PACK_HASH.isEmpty()) {
                    ResourcePack resourcePack = ResourcePack.optional(RESOURCE_PACK_URL, RESOURCE_PACK_HASH);
                    event.getPlayer().setResourcePack(resourcePack);
                }
            }
        });

        registerCommands();
    }

    private void registerCommands() {
        CommandManager commands = MinecraftServer.getCommandManager();
        commands.register(new StatusCommand());
        commands.register(new TestsCommand());

        commands.register(new CanaryCommand());
        commands.register(new TestCommand());
        commands.register(new InstanceCommand());
    }
}
