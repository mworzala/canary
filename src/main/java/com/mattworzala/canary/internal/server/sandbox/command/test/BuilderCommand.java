package com.mattworzala.canary.internal.server.sandbox.command.test;

import com.mattworzala.canary.internal.server.sandbox.SandboxServer;
import com.mattworzala.canary.internal.structure.Structure;
import com.mattworzala.canary.internal.structure.StructureFilesUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.mattworzala.canary.internal.server.sandbox.command.TestCommand.version;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class BuilderCommand extends Command {
    private static final String NAME = "builder";
    private static final String VERSION = "0.0.1";

    private final SandboxServer server;

    private final ItemStack testBuilderItem;

    private ArgumentString structureName = ArgumentType.String("structure-name");
    private ArgumentWord structureFile = ArgumentType.Word("existing-structure-file");
    private ArgumentWord currentTestBuilders = ArgumentType.Word("test-builder-id");

    public BuilderCommand(SandboxServer server) {
        super("builder", "b");
        this.server = server;

        this.testBuilderItem = getTestBuilderItem();

//        setDefaultExecutor(this::onBuild);

        CommandCondition inTestCondition = (sender, commandString) -> server.playerInTestBuilder(sender.asPlayer());
        CommandCondition notInTestCondition = (sender, commandString) -> !inTestCondition.canUse(sender, commandString);

        addConditionalSyntax(inTestCondition, this::onDone, Literal("done"));

        addConditionalSyntax(notInTestCondition, this::onNewTest, Literal("new"), structureName);
        addConditionalSyntax(notInTestCondition, this::onDuplicate, Literal("duplicate"), structureFile.from(getExistingStructureNames()), structureName);
        addConditionalSyntax(notInTestCondition, this::onBuild, Literal("import"));
        updateCurrentTestBuilders();
        addConditionalSyntax(notInTestCondition, this::onJoin, Literal("join"), currentTestBuilders);

    }

    private String[] getExistingStructureNames() {
        return StructureFilesUtil.getStructureFiles().toArray(new String[0]);
    }

    private ItemStack getTestBuilderItem() {
        return ItemStack.builder(Material.BOOK)
                .displayName(Component.text("Test Builder", NamedTextColor.GREEN))
                .build();
    }

    private void onNewTest(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        final String name = commandContext.get(structureName);

        server.newTestBuilder(name, commandSender.asPlayer());
        commandSender.asPlayer().sendMessage("Making a new structure with name \"" + name + "\"");

        updateCurrentTestBuilders();
        commandSender.asPlayer().refreshCommands();
    }

    private void onDone(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        server.playerDoneInTestBuilder(commandSender.asPlayer());

        updateCurrentTestBuilders();
        commandSender.asPlayer().refreshCommands();

    }

    private void onDuplicate(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        final String structureFile = commandContext.get("existing-structure-file");
        final String name = commandContext.get(structureName);

        Player player = commandSender.asPlayer();

        server.newTestBuilder(name, player, StructureFilesUtil.structureFromFile(structureFile));
        player.sendMessage("duplicating structure " + structureFile + " into new structure with name \"" + name + "\"");

        updateCurrentTestBuilders();
        player.refreshCommands();
    }

    private void onJoin(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        String testBuilderId = commandContext.get("test-builder-id");

        Player player = commandSender.asPlayer();
        server.addPlayerToTestBuilder(player, testBuilderId);

        player.refreshCommands();
    }

    private void onBuild(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        new Thread(() -> {
            Player player = commandSender.asPlayer();

            player.getInventory().setItemInMainHand(this.testBuilderItem);

            EventNode<Event> node = EventNode.all("test-builder");
            var handler = MinecraftServer.getGlobalEventHandler();

            TestBuilder testBuilder = new TestBuilder(player, commandSender::sendMessage);
            CountDownLatch builderFinished = new CountDownLatch(1);
            // TODO - fix this to make it not wack
            node.addListener(EventListener.builder(PlayerUseItemOnBlockEvent.class)
                    .expireWhen(event -> {
                        if (testBuilder.isDoneBuilding()) {
                            builderFinished.countDown();
                            return true;
                        }
                        return false;
                    })
                    .handler((event) -> {
                        if (event.getPlayer().equals(player) && event.getItemStack().equals(this.testBuilderItem)) {
                            Point pos = event.getPosition();
//                            commandSender.sendMessage("you clicked on a block at position: " + pos);
                            testBuilder.handleTestBuilderSelect(pos);
                        }
                    }).build());

            handler.addChild(node);

            try {
                builderFinished.await();
                System.out.println("Builder finished!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            final var playerInstance = player.getInstance();

            commandSender.sendMessage("making a structure with origin: " + testBuilder.getOrigin() + " and size: " + testBuilder.getSize());

            Structure structure = Structure.structureFromWorld(playerInstance, "structure-id", testBuilder.getOrigin(), testBuilder.getSize());

            server.newTestBuilder("test-test", player, structure);

            updateCurrentTestBuilders();
            player.refreshCommands();

        }).start();
    }

    private void onHelp(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);

        sender.sendMessage("Test builder help...");
    }

    private void updateCurrentTestBuilders() {
        List<String> testBuilderNames = server.getExistingTestBuilders();
        if (testBuilderNames.size() == 0) {
            currentTestBuilders = currentTestBuilders.from(null);
        } else {
            currentTestBuilders = currentTestBuilders.from(testBuilderNames.toArray(new String[0]));
        }
    }
}