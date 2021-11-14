package com.mattworzala.canary.internal.server.sandbox;

import com.mattworzala.canary.internal.server.HeadlessServer;
import com.mattworzala.canary.internal.server.sandbox.command.*;
import com.mattworzala.canary.internal.server.sandbox.testbuilder.TestBuilderController;
import com.mattworzala.canary.internal.structure.Structure;
import com.mattworzala.canary.internal.util.safety.Env;
import com.mattworzala.canary.internal.util.safety.EnvType;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.PlacementRules;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.resourcepack.ResourcePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Env(EnvType.MINESTOM)
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

    private List<TestBuilderController> testBuilderControllers = new ArrayList<>();
    private List<UUID> playerUUIDInTestBuilder = new ArrayList<>();


    @Override
    public void initServer() {
        super.initServer();
        headless = true;

        // Util options
        OptifineSupport.enable();
        PlacementRules.init();
//        MojangAuth.init();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(getTestCoordinator().getInstance());
            player.setRespawnPoint(new Pos(0, 41, 0));
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
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

        commands.register(new CanaryCommand());
        commands.register(new TestCommand(this));
        commands.register(new InstanceCommand());
        commands.register(new EntityCommand());
        commands.register(PromptCommand.getInstance());
    }

    /**
     * Make a new test builder, add the given player to it
     *
     * @param name   Name for the test
     * @param player The player to put into the test builder
     */
    public void newTestBuilder(String name, Player player) {
        TestBuilderController controller = getNewTestBuilderController(name);
        player.sendMessage("there are " + testBuilderControllers.size() + " current test builder controllers");
        controller.addPlayer(player);
        playerUUIDInTestBuilder.add(player.getUuid());
    }

    /**
     * Make a new test builder, add the given player to it, import the given structure
     *
     * @param name      Name for the test
     * @param player    The player to put into the test builder
     * @param structure The structure to import into the test builder
     */
    public void newTestBuilder(String name, Player player, Structure structure) {
        TestBuilderController controller = getNewTestBuilderController(name);
        player.sendMessage("there are " + testBuilderControllers.size() + " current test builder controllers");
        controller.importStructure(structure);
        controller.addPlayer(player);
        playerUUIDInTestBuilder.add(player.getUuid());
    }

    /**
     * Adds the player to the existing test builder with the given name.
     * If there is no test builder with that name, nothing happens
     *
     * @param player
     * @param name
     */
    public void addPlayerToTestBuilder(Player player, String name) {
        for (TestBuilderController testBuilder : testBuilderControllers) {
            if (testBuilder.getName().equals(name)) {
                playerUUIDInTestBuilder.add(player.getUuid());
                testBuilder.addPlayer(player);
            }
        }
    }

    public void playerDoneInTestBuilder(Player player) {
        for (TestBuilderController testBuilder : testBuilderControllers) {
            if (testBuilder.hasPlayer(player)) {
                playerUUIDInTestBuilder.remove(player.getUuid());
                // TODO - make this just remove the player, not finish the test builder, unless they're the last player
                testBuilder.finish();

                if (testBuilder.getPlayers().size() == 0) {
                    if (testBuilderControllers.size() > 1) {
                        testBuilder.unregister();
                        testBuilderControllers.remove(testBuilder);
                    } else {
                        testBuilder.reset();
                    }
                }

                return;
            }
        }
    }


    /**
     * @param name
     * @return A test builder controller with the given name, that is in the controller list
     */
    private TestBuilderController getNewTestBuilderController(String name) {
        for (TestBuilderController controller : testBuilderControllers) {
            if (controller.getPlayers().size() == 0) {
                controller.setName(name);
                return controller;
            }
        }
        var testBuilderController = new TestBuilderController(name);
        testBuilderControllers.add(testBuilderController);
        return testBuilderController;
    }


    /**
     * @return The names of all the currently active test builders
     */
    public List<String> getExistingTestBuilders() {
        return testBuilderControllers.stream().map(TestBuilderController::getName).collect(Collectors.toList());
    }

    public boolean playerInTestBuilder(Player p) {
        return playerUUIDInTestBuilder.contains(p.getUuid());
    }

}
