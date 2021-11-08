package com.mattworzala.canary.internal.server.sandbox.command.test;

import com.mattworzala.canary.internal.server.sandbox.testbuilder.TestBuilderController;
import com.mattworzala.canary.internal.structure.Structure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
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

import java.util.concurrent.CountDownLatch;

import static com.mattworzala.canary.internal.server.sandbox.command.TestCommand.version;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class BuilderCommand extends Command {
    private static final String NAME = "builder";
    private static final String VERSION = "0.0.1";

    private ItemStack testBuilderItem;
    private TestBuilderController testBuilderController;
    private boolean isPlayerInTestBuilder = false;

    public BuilderCommand() {
        super("builder", "b");

//        setDefaultExecutor(this::onBuild);
//        setDefaultExecutor(this::onNewTest);

        var structureName = ArgumentType.String("structure-name");
        CommandCondition inTestCondition = (sender, commandString) -> isPlayerInTestBuilder;
        CommandCondition notInTestCondition = (sender, commandString) -> !isPlayerInTestBuilder;
        addConditionalSyntax(inTestCondition, ((sender, context) -> {
            if (testBuilderController != null) {
                sender.asPlayer().sendMessage("Done making structure \"" + testBuilderController.getName() + "\"");
                testBuilderController.finish();
                testBuilderController = null;
                isPlayerInTestBuilder = false;
                sender.asPlayer().refreshCommands();
            } else {
                sender.asPlayer().sendMessage("\"test builder done\" is used to save a structure, you are not currently in a structure");
            }
        }), Literal("done"));

        addConditionalSyntax(notInTestCondition, ((sender, context) -> {
            if (testBuilderController != null) {
                sender.asPlayer().sendMessage("You are currently building structure \"" + testBuilderController.getName() + "\"");
            }
            final String name = context.get(structureName);
            testBuilderController = new TestBuilderController(name);
            testBuilderController.addPlayer(sender.asPlayer());
            isPlayerInTestBuilder = true;
            sender.asPlayer().sendMessage("Making a new structure with name \"" + name + "\"");
            sender.asPlayer().refreshCommands();
        }), Literal("new"), structureName);

        addConditionalSyntax(notInTestCondition, ((sender, context) -> {
            final String name = context.get(structureName);
            testBuilderController = new TestBuilderController(name);
            testBuilderController.addPlayer(sender.asPlayer());
            isPlayerInTestBuilder = true;
            sender.asPlayer().sendMessage("Making a new structure with name \"" + name + "\"");
            sender.asPlayer().refreshCommands();
        }), Literal("duplicate"), ArgumentType.Word("existing-structure-id").from(getExistingStructureNames()), structureName);

        addConditionalSyntax(notInTestCondition, this::onBuild, Literal("import"));
        this.testBuilderItem = getTestBuilderItem();

    }

    private String[] getExistingStructureNames() {
        return new String[]{"test1", "test2", "test3"};
    }

    private ItemStack getTestBuilderItem() {
        return ItemStack.builder(Material.BOOK)
                .displayName(Component.text("Test Builder", NamedTextColor.GREEN))
                .build();
    }

    private void onNewTest(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
//        Player player = commandSender.asPlayer();
//        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
//
//        testBuilderController = new TestBuilderController(player);
    }

    private void onBuild(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        new Thread(() -> {
            Player player = commandSender.asPlayer();

            player.getInventory().setItemInMainHand(this.testBuilderItem);

            EventNode<Event> node = EventNode.all("test-builder");
            var handler = MinecraftServer.getGlobalEventHandler();

            TestBuilder testBuilder = new TestBuilder(player, commandSender::sendMessage);
            CountDownLatch builderFinished = new CountDownLatch(1);
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

            testBuilderController = new TestBuilderController("test-test");
            testBuilderController.addPlayer(player);
            testBuilderController.importStructure(structure);
            isPlayerInTestBuilder = true;
//            player.sendMessage("Making a new structure with name \"" + name + "\"");
            player.refreshCommands();

//            // TODO - don't do this, actually go somewhere reasonable
//            Path root = FileSystems.getDefault().getPath("..").toAbsolutePath();
//            Path filePath = Paths.get(root.toString(), "src", "main", "resources", "test.json");
//            StructureWriter structureWriter = new JsonStructureIO();
//            structureWriter.writeStructure(structure, filePath);
        }).start();
    }

//    private Structure readStructureFromWorld(Instance instance, Point origin, Point size) {
//        return readStructureFromWorld(instance, origin, size.blockX(), size.blockY(), size.blockZ());
//    }
//
//    private Structure readStructureFromWorld(Instance instance, Point origin, int sizeX, int sizeY, int sizeZ) {
//        Structure resultStructure = new Structure("testStruct", sizeX, sizeY, sizeZ);
//
//        Set<Block> blockSet = new HashSet<>();
//        blockSet.add(Block.AIR);
//        Map<Integer, Block> blockMap = new HashMap<>();
//
//        blockMap.put(-1, Block.AIR);
//        int blockMapIndex = 0;
//        Block lastBlock = null;
//        int lastBlockIndex = -1;
//        int currentBlockCount = 0;
//
//        for (int y = 0; y < sizeY; y++) {
//            for (int z = 0; z < sizeZ; z++) {
//                for (int x = 0; x < sizeX; x++) {
////                    System.out.println("(" + (origin.blockX() + x) + ", " + (origin.blockY() + y) + ", " + (origin.blockZ() + z));
//                    Block b = instance.getBlock(origin.blockX() + x, origin.blockY() + y, origin.blockZ() + z, BlockGetter.Condition.NONE);
//                    // if this is the very first block
//                    if (lastBlock == null) {
//                        if (blockSet.add(b)) {
//                            // if this is a new block we haven't seen before
//                            // put it in the block map
//                            blockMap.put(blockMapIndex, b);
//                            blockMapIndex++;
//                            lastBlockIndex = 0;
//
//                            lastBlock = b;
//                        } else {
//                            lastBlock = b;
//                            lastBlockIndex = -1;
//                        }
//                        currentBlockCount++;
//                    } else {
//                        if (b.equals(lastBlock)) {
//                            currentBlockCount++;
//                        } else {
//                            resultStructure.addToBlockDefList(new Structure.BlockDef(lastBlockIndex, currentBlockCount));
//                            currentBlockCount = 1;
//                            if (blockSet.add(b)) {
//                                // if this is a new block we haven't seen before
//                                // put it in the block map
//                                blockMap.put(blockMapIndex, b);
//                                lastBlockIndex = blockMapIndex;
//                                blockMapIndex++;
//
//                                lastBlock = b;
//                            } else {
//                                lastBlock = b;
//                                for (int key : blockMap.keySet()) {
//                                    if (blockMap.get(key).equals(b)) {
//                                        lastBlockIndex = key;
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        resultStructure.addToBlockDefList(new Structure.BlockDef(lastBlockIndex, currentBlockCount));
//
//        for (int key : blockMap.keySet()) {
//            resultStructure.putInBlockMap(key, blockMap.get(key));
//        }
//
//        return resultStructure;
//    }

    private void onHelp(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);

        sender.sendMessage("Test builder help...");
    }
}