package com.mattworzala.canary.server;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.command.*;
import com.mattworzala.canary.server.givemeahome.SandboxTestCoordinator;
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

@Environment(EnvType.MINESTOM)
public class SandboxServer extends HeadlessServer {
    private final SandboxTestCoordinator coordinator = new SandboxTestCoordinator();

    @Override
    public void initServer() {
        super.initServer();


        // Util options
        OptifineSupport.enable();
        PlacementRules.init();
        MojangAuth.init();

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addEventCallback(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(coordinator.getSandboxViewer());
            player.setRespawnPoint(new Pos(0, 41, 0));
        });
        globalEventHandler.addEventCallback(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) {
                event.getPlayer().setGameMode(GameMode.CREATIVE);
                event.getPlayer().setPermissionLevel(4);
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
