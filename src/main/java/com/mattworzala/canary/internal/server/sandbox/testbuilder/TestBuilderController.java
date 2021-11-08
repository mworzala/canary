package com.mattworzala.canary.internal.server.sandbox.testbuilder;

import com.mattworzala.canary.internal.server.instance.block.BoundingBoxHandler;
import com.mattworzala.canary.internal.structure.JsonStructureIO;
import com.mattworzala.canary.internal.structure.Structure;
import com.mattworzala.canary.internal.structure.StructureWriter;
import com.mattworzala.canary.internal.util.testbuilder.BlockBoundingBox;
import com.mattworzala.canary.internal.util.ui.BlockClickingItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockGetter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.world.DimensionType;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

// REFACTOR : Allow multiple players per test builder
// REFACTOR : Allow multiple concurrent test builders
public class TestBuilderController {

    private static final int MAX_STRUCTURE_DIMENSION = 48;
    // put the structure block as low as possible (48 blocks down) without going into negative y
    private static final int MAX_STRUCTURE_BLOCK_OFFSET = 48;

    private BlockBoundingBox blockBoundingBox = new BlockBoundingBox(MAX_STRUCTURE_DIMENSION);

    private Point structureBlockPos;
    private Block lastOverwrittenBlock;

    private Instance testBuilderInstance;

    private Player player;
    private Instance playerPreviousInstance;
    private Point playerPreviousInstancePos;

    private static final EventNode<PlayerEvent> testBuilderPlayerEventNode = EventNode.type("test-builder-controller-player", EventFilter.PLAYER);

    static {
        MinecraftServer.getGlobalEventHandler().addChild(testBuilderPlayerEventNode);
    }

    private String name;

    public TestBuilderController(String name) {
        this.name = name;

        testBuilderInstance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        MinecraftServer.getInstanceManager().registerInstance(testBuilderInstance);

        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                var point = new Vec(x, 40, z);
                blockBoundingBox.addBlock(point);
                testBuilderInstance.setBlock(point, Block.STONE);
            }
        }
    }

    public void addMarker(Point p) {
    }

    public void addPlayer(Player player) {
        this.player = player;
        playerPreviousInstance = player.getInstance();
        playerPreviousInstancePos = player.getPosition();
        player.setInstance(testBuilderInstance, new Vec(0, 41, 0));
        System.out.println("tried to set the player instance");

        // REFACTOR : Register events once in constructor in test builder controller
        testBuilderPlayerEventNode.addListener(EventListener.builder(PlayerBlockPlaceEvent.class)
                .filter(event -> event.getPlayer().equals(player))
                .handler(playerBlockPlaceEvent -> {
                    System.out.println("PLAYER BLOCK PLACE EVENT");
                    System.out.println("BLOCK POS: " + playerBlockPlaceEvent.getBlockPosition());
                    if (blockBoundingBox.addBlock(playerBlockPlaceEvent.getBlockPosition())) {
                        this.updateStructureOutline();
                    } else {
                        playerBlockPlaceEvent.setCancelled(true);
                    }
                }).build());

        testBuilderPlayerEventNode.addListener(EventListener.builder(PlayerBlockBreakEvent.class)
                .filter(event -> event.getPlayer().equals(player))
                .handler(playerBlockBreakEvent -> {
                    System.out.println("PLAYER BLOCK BREAK EVENT");
                    System.out.println("BLOCK POS: " + playerBlockBreakEvent.getBlockPosition());
                    blockBoundingBox.removeBlock(playerBlockBreakEvent.getBlockPosition());
                    this.updateStructureOutline();
                }).build());

        var itemStack = ItemStack.builder(Material.BOOK)
                .displayName(Component.text("Test Builder", NamedTextColor.GREEN))
                .build();

        Function<Point, Boolean> leftClick = (p) -> {
            System.out.println("LEFT CLICK");
            return false;
        };

        Function<Point, Boolean> rightClick = (p) -> {
            System.out.println("RIGHT CLICK");
            return false;
        };
        BlockClickingItemStack bcis = new BlockClickingItemStack(itemStack, leftClick, rightClick);
        bcis.giveToPlayer(player, player.getHeldSlot());
    }

    // REFACTOR : Prompt to save when leaving test builder
    public void finish() {
        System.out.println("FINISHING BUILDING STRUCTURE: " + name);
        player.setInstance(playerPreviousInstance, playerPreviousInstancePos);

        Structure structure = this.readStructureFromWorld(blockBoundingBox.getMinPoint(), blockBoundingBox.getSize());

        Path root = FileSystems.getDefault().getPath("..").toAbsolutePath();
        Path filePath = Paths.get(root.toString(), "src", "main", "resources", name + ".json");
        StructureWriter structureWriter = new JsonStructureIO();
        structureWriter.writeStructure(structure, filePath);
    }

    private void updateStructureOutline() {
        if (structureBlockPos == null) {
            recomputeStructureBlockPos();
        } else {
            Point minPoint = blockBoundingBox.getMinPoint();

            Point size = blockBoundingBox.getSize();

            Point structureBlockOffset = minPoint.sub(structureBlockPos);
            int x = structureBlockOffset.blockX();
            int y = structureBlockOffset.blockY();
            int z = structureBlockOffset.blockZ();

            if (x <= MAX_STRUCTURE_BLOCK_OFFSET &&
                    y <= MAX_STRUCTURE_BLOCK_OFFSET &&
                    z <= MAX_STRUCTURE_BLOCK_OFFSET) {
                // if the structure block doesn't need to move, we just update the size and offset
                System.out.println("Don't need to move structure block, updating offset");
                Block boundingBox = boundingBoxBlockFromSizeAndPos(size, structureBlockOffset);
                BlockEntityDataPacket blockEntityDataPacket = new BlockEntityDataPacket();

                blockEntityDataPacket.blockPosition = structureBlockPos;
                blockEntityDataPacket.action = 7;
                blockEntityDataPacket.nbtCompound = boundingBox.nbt();

                player.sendPacket(blockEntityDataPacket);
            } else {
                // the structure block does need to move
                System.out.println("Do need to move structure block");
                recomputeStructureBlockPos();
            }

        }
    }

    /**
     * Fully calculates the position of the structure block,
     * removes the current structure block if it exists, and sends update to player
     */
    private void recomputeStructureBlockPos() {
        Point minPoint = blockBoundingBox.getMinPoint();
        Point size = blockBoundingBox.getSize();

        int structureBlockYPos = minPoint.blockY() <= MAX_STRUCTURE_BLOCK_OFFSET ? 0 : minPoint.blockY() - MAX_STRUCTURE_BLOCK_OFFSET;
        int structureBlockYOffset = minPoint.blockY() - structureBlockYPos;

        Block boundingBox = boundingBoxBlockFromSizeAndYPos(size, structureBlockYOffset);
        BlockEntityDataPacket blockEntityDataPacket = new BlockEntityDataPacket();

        Point blockPos = minPoint.add(new Vec(0, -structureBlockYOffset, 0));
        blockEntityDataPacket.blockPosition = blockPos;
        blockEntityDataPacket.action = 7;
        blockEntityDataPacket.nbtCompound = boundingBox.nbt();

        if (lastOverwrittenBlock != null) {
            testBuilderInstance.setBlock(structureBlockPos, lastOverwrittenBlock);
        }
        lastOverwrittenBlock = testBuilderInstance.getBlock(blockPos);
        structureBlockPos = blockPos;
        testBuilderInstance.setBlock(blockPos, boundingBox);

        player.sendPacket(blockEntityDataPacket);
    }

    private Block boundingBoxBlockFromSizeAndPos(Point size, Point pos) {
        return BoundingBoxHandler.BLOCK
                .withTag(BoundingBoxHandler.Tags.SizeX, size.blockX())
                .withTag(BoundingBoxHandler.Tags.SizeY, size.blockY())
                .withTag(BoundingBoxHandler.Tags.SizeZ, size.blockZ())
                .withTag(BoundingBoxHandler.Tags.PosX, pos.blockX())
                .withTag(BoundingBoxHandler.Tags.PosY, pos.blockY())
                .withTag(BoundingBoxHandler.Tags.PosZ, pos.blockZ());
    }

    private Block boundingBoxBlockFromSizeAndYPos(Point size, int yPos) {
        return boundingBoxBlockFromSizeAndPos(size, new Vec(0, yPos, 0));
    }


    private Structure readStructureFromWorld(Point origin, Point size) {
        return readStructureFromWorld(origin, size.blockX(), size.blockY(), size.blockZ());
    }

    private Structure readStructureFromWorld(Point origin, int sizeX, int sizeY, int sizeZ) {
        Structure resultStructure = new Structure("testStruct", sizeX, sizeY, sizeZ);

        Set<Block> blockSet = new HashSet<>();
        blockSet.add(Block.AIR);
        Map<Integer, Block> blockMap = new HashMap<>();

        blockMap.put(-1, Block.AIR);
        int blockMapIndex = 0;
        Block lastBlock = null;
        int lastBlockIndex = -1;
        int currentBlockCount = 0;

        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
//                    System.out.println("(" + (origin.blockX() + x) + ", " + (origin.blockY() + y) + ", " + (origin.blockZ() + z));
                    Block b = this.testBuilderInstance.getBlock(origin.blockX() + x, origin.blockY() + y, origin.blockZ() + z, BlockGetter.Condition.NONE);
                    // if this is the very first block
                    if (lastBlock == null) {
                        if (blockSet.add(b)) {
                            // if this is a new block we haven't seen before
                            // put it in the block map
                            blockMap.put(blockMapIndex, b);
                            blockMapIndex++;
                            lastBlockIndex = 0;

                            lastBlock = b;
                        } else {
                            lastBlock = b;
                            lastBlockIndex = -1;
                        }
                        currentBlockCount++;
                    } else {
                        if (b.equals(lastBlock)) {
                            currentBlockCount++;
                        } else {
                            resultStructure.addToBlockDefList(new Structure.BlockDef(lastBlockIndex, currentBlockCount));
                            currentBlockCount = 1;
                            if (blockSet.add(b)) {
                                // if this is a new block we haven't seen before
                                // put it in the block map
                                blockMap.put(blockMapIndex, b);
                                lastBlockIndex = blockMapIndex;
                                blockMapIndex++;

                                lastBlock = b;
                            } else {
                                lastBlock = b;
                                for (int key : blockMap.keySet()) {
                                    if (blockMap.get(key).equals(b)) {
                                        lastBlockIndex = key;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        resultStructure.addToBlockDefList(new Structure.BlockDef(lastBlockIndex, currentBlockCount));

        for (int key : blockMap.keySet()) {
            resultStructure.putInBlockMap(key, blockMap.get(key));
        }

        return resultStructure;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instance getTestBuilderInstance() {
        return testBuilderInstance;
    }
}
