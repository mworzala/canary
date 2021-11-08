package com.mattworzala.canary.internal.server.sandbox.testbuilder;

import com.mattworzala.canary.internal.server.instance.block.BoundingBoxHandler;
import com.mattworzala.canary.internal.structure.JsonStructureIO;
import com.mattworzala.canary.internal.structure.Structure;
import com.mattworzala.canary.internal.structure.StructureWriter;
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

    private Point minPoint;
    private Point maxPoint;

    // REFACTOR : Move this to a separate class that doesnt reference minestom classes
    private int[] xBlockCounts = new int[MAX_STRUCTURE_DIMENSION];
    private int[] yBlockCounts = new int[MAX_STRUCTURE_DIMENSION];
    private int[] zBlockCounts = new int[MAX_STRUCTURE_DIMENSION];

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

        minPoint = new Vec(0, 40, 0);
        maxPoint = new Vec(5, 41, 5);
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                var point = new Vec(x, 40, z);
                addPositionToBlockCoordLists(point);
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
                    if (addPositionToBlockCoordLists(playerBlockPlaceEvent.getBlockPosition())) {
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
                    removePositionFromBlockCoordLists(playerBlockBreakEvent.getBlockPosition());
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

//        Point minPoint = this.minPoint;
//        Point maxPoint = this.getMaxPoint();

        int sizeX = maxPoint.blockX() - minPoint.blockX() + 1;
        int sizeY = maxPoint.blockY() - minPoint.blockY() + 1;
        int sizeZ = maxPoint.blockZ() - minPoint.blockZ() + 1;

        Structure structure = this.readStructureFromWorld(minPoint, sizeX, sizeY, sizeZ);

        Path root = FileSystems.getDefault().getPath("..").toAbsolutePath();
        Path filePath = Paths.get(root.toString(), "src", "main", "resources", name + ".json");
        StructureWriter structureWriter = new JsonStructureIO();
        structureWriter.writeStructure(structure, filePath);
    }

    /**
     * @param point
     * @return true if block can be successfully added, false if it cannot be
     */
    private boolean addPositionToBlockCoordLists(Point point) {
        Point newMin = minOfPoints(minPoint, point);
        Point newMax = maxOfPoints(maxPoint, point);
        Point newSize = newMax.sub(newMin);
        // if the new block is in a position that cannot be encapsulated by a structure bounding box, don't insert
        if (newSize.blockX() > MAX_STRUCTURE_DIMENSION || newSize.blockZ() > MAX_STRUCTURE_DIMENSION || newSize.blockY() > MAX_STRUCTURE_DIMENSION) {
            return false;
        }

        Point delta = minPoint.sub(newMin);
        if (delta.blockX() < 0 || delta.blockY() < 0 || delta.blockZ() < 0) {
            System.out.println("delta had a negative component, which can't happen");
            return false;
        }

        shiftArray(xBlockCounts, delta.blockX());
        shiftArray(yBlockCounts, delta.blockY());
        shiftArray(zBlockCounts, delta.blockZ());
        minPoint = newMin;
        maxPoint = newMax;
        Point offset = point.sub(minPoint);
        xBlockCounts[offset.blockX()] += 1;
        yBlockCounts[offset.blockY()] += 1;
        zBlockCounts[offset.blockZ()] += 1;
        return true;
    }

    /**
     * Shifts the elements in the array to the right by the shiftAmount
     * Fills the empty space on the left with zeros
     * If shiftAmount is negative does a left shift, filling the right with zeros
     *
     * @param arr
     * @param shiftAmount
     */
    private void shiftArray(int[] arr, int shiftAmount) {
        if (shiftAmount == 0)
            return;

        if (shiftAmount > 0) {
            int i;
            for (i = arr.length - 1; i >= shiftAmount; i--) {
                arr[i] = arr[i - shiftAmount];
            }
            for (; i >= 0; i--) {
                arr[i] = 0;
            }
        } else {
            int i;
            for (i = 0; i < (arr.length + shiftAmount); i++) {
                arr[i] = arr[i - shiftAmount];
            }
            for (; i < arr.length; i++) {
                arr[i] = 0;
            }

        }
    }

    private void removePositionFromBlockCoordLists(Point point) {
        Point offset = point.sub(minPoint);
        xBlockCounts[offset.blockX()] -= 1;
        yBlockCounts[offset.blockY()] -= 1;
        zBlockCounts[offset.blockZ()] -= 1;

        int deltaX = firstNonZero(xBlockCounts);
        shiftArray(xBlockCounts, -deltaX);
        minPoint = minPoint.withX(minPoint.x() + deltaX);
        int deltaY = firstNonZero(yBlockCounts);
        shiftArray(yBlockCounts, -deltaY);
        minPoint = minPoint.withY(minPoint.y() + deltaY);
        int deltaZ = firstNonZero(zBlockCounts);
        shiftArray(zBlockCounts, -deltaZ);
        minPoint = minPoint.withZ(minPoint.z() + deltaZ);

        recomputeMaxPoint();
    }

    private void recomputeMaxPoint() {
        Point offset = maxPoint.sub(minPoint);
        for (int x = offset.blockX(); x >= 0; x--) {
            if (xBlockCounts[x] != 0) {
                maxPoint = maxPoint.withX(minPoint.x() + x);
                break;
            }
        }
        for (int y = offset.blockY(); y >= 0; y--) {
            if (yBlockCounts[y] != 0) {
                maxPoint = maxPoint.withY(minPoint.y() + y);
                break;
            }
        }
        for (int z = offset.blockZ(); z >= 0; z--) {
            if (zBlockCounts[z] != 0) {
                maxPoint = maxPoint.withZ(minPoint.z() + z);
                break;
            }
        }
    }

    private int firstNonZero(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0) {
                return i;
            }
        }
        return arr.length;
    }

    private void updateStructureOutline() {
        System.out.println("minPoint: " + minPoint);
        System.out.println("maxPoint: " + maxPoint);
        System.out.println("xBlockCounts: " + Arrays.toString(xBlockCounts));
        System.out.println("yBlockCounts: " + Arrays.toString(yBlockCounts));
        System.out.println("zBlockCounts: " + Arrays.toString(zBlockCounts));
        Point minPoint = this.minPoint;
        Point maxPoint = this.maxPoint;

        int sizeX = maxPoint.blockX() - minPoint.blockX() + 1;
        int sizeY = maxPoint.blockY() - minPoint.blockY() + 1;
        int sizeZ = maxPoint.blockZ() - minPoint.blockZ() + 1;

        // put the structure block as low as possible (48 blocks down) without going into negative y
        final int MAX_STRUCTURE_BLOCK_OFFSET = 48;
        int structureBlockYPos = minPoint.blockY() <= MAX_STRUCTURE_BLOCK_OFFSET ? 0 : minPoint.blockY() - MAX_STRUCTURE_BLOCK_OFFSET;
        int structureBlockYOffset = minPoint.blockY() - structureBlockYPos;

        // TODO - clean this up and remove the duplication, also do any amount of safety checks

        // if this is our first time placing the structure block
        if (structureBlockPos == null) {
            var boundingBox = BoundingBoxHandler.BLOCK
                    .withTag(BoundingBoxHandler.Tags.SizeX, sizeX)
                    .withTag(BoundingBoxHandler.Tags.SizeY, sizeY)
                    .withTag(BoundingBoxHandler.Tags.SizeZ, sizeZ)
                    .withTag(BoundingBoxHandler.Tags.PosY, structureBlockYOffset);
            BlockEntityDataPacket blockEntityDataPacket = new BlockEntityDataPacket();

            Point blockPos = minPoint.add(new Vec(0, -structureBlockYOffset, 0));
            blockEntityDataPacket.blockPosition = blockPos;
            blockEntityDataPacket.action = 7;
            blockEntityDataPacket.nbtCompound = boundingBox.nbt();

            player.sendPacket(blockEntityDataPacket);

            System.out.println("first time placing structure block");
            if (lastOverwrittenBlock != null) {
                testBuilderInstance.setBlock(structureBlockPos, lastOverwrittenBlock);
            }
            lastOverwrittenBlock = testBuilderInstance.getBlock(blockPos);
            structureBlockPos = blockPos;
            testBuilderInstance.setBlock(blockPos, boundingBox);
        } else {
            Point structureBlockOffset = minPoint.sub(structureBlockPos);
            int x = structureBlockOffset.blockX();
            int y = structureBlockOffset.blockY();
            int z = structureBlockOffset.blockZ();

            if (x <= MAX_STRUCTURE_BLOCK_OFFSET &&
                    y <= MAX_STRUCTURE_BLOCK_OFFSET &&
                    z <= MAX_STRUCTURE_BLOCK_OFFSET) {
                // if the structure block doesn't need to move
                System.out.println("Don't need to move structure block, updating offset");
                var boundingBox = BoundingBoxHandler.BLOCK
                        .withTag(BoundingBoxHandler.Tags.SizeX, sizeX)
                        .withTag(BoundingBoxHandler.Tags.SizeY, sizeY)
                        .withTag(BoundingBoxHandler.Tags.SizeZ, sizeZ)
                        .withTag(BoundingBoxHandler.Tags.PosX, x)
                        .withTag(BoundingBoxHandler.Tags.PosZ, z)
                        .withTag(BoundingBoxHandler.Tags.PosY, y);
                BlockEntityDataPacket blockEntityDataPacket = new BlockEntityDataPacket();

//                Point blockPos = minPoint.add(new Vec(0, -structureBlockYOffset, 0));
                blockEntityDataPacket.blockPosition = structureBlockPos;
                blockEntityDataPacket.action = 7;
                blockEntityDataPacket.nbtCompound = boundingBox.nbt();

                player.sendPacket(blockEntityDataPacket);
            } else {
                // the structure block does need to move
                System.out.println("Do need to move structure block");
                var boundingBox = BoundingBoxHandler.BLOCK
                        .withTag(BoundingBoxHandler.Tags.SizeX, sizeX)
                        .withTag(BoundingBoxHandler.Tags.SizeY, sizeY)
                        .withTag(BoundingBoxHandler.Tags.SizeZ, sizeZ)
                        .withTag(BoundingBoxHandler.Tags.PosY, structureBlockYOffset);
                BlockEntityDataPacket blockEntityDataPacket = new BlockEntityDataPacket();

                Point blockPos = minPoint.add(new Vec(0, -structureBlockYOffset, 0));
                blockEntityDataPacket.blockPosition = blockPos;
                blockEntityDataPacket.action = 7;
                blockEntityDataPacket.nbtCompound = boundingBox.nbt();

                player.sendPacket(blockEntityDataPacket);

                if (lastOverwrittenBlock != null) {
                    testBuilderInstance.setBlock(structureBlockPos, lastOverwrittenBlock);
                }
                lastOverwrittenBlock = testBuilderInstance.getBlock(blockPos);
                structureBlockPos = blockPos;
                testBuilderInstance.setBlock(blockPos, boundingBox);
            }

        }
    }

    private Point minOfPoints(Point p1, Point p2) {
        return new Vec(Math.min(p1.x(), p2.x()), Math.min(p1.y(), p2.y()), Math.min(p1.z(), p2.z()));
    }

    private Point maxOfPoints(Point p1, Point p2) {
        return new Vec(Math.max(p1.x(), p2.x()), Math.max(p1.y(), p2.y()), Math.max(p1.z(), p2.z()));
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
