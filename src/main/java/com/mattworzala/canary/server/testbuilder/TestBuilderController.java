package com.mattworzala.canary.server.testbuilder;

import com.mattworzala.canary.server.instance.block.BoundingBoxHandler;
import com.mattworzala.canary.server.structure.JsonStructureIO;
import com.mattworzala.canary.server.structure.Structure;
import com.mattworzala.canary.server.structure.StructureWriter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockGetter;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.world.DimensionType;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TestBuilderController {

    private static final int MAX_STRUCTURE_DIMENSION = 45;

    private List<Integer> blockXCoords = new ArrayList<>();
    private List<Integer> blockYCoords = new ArrayList<>();
    private List<Integer> blockZCoords = new ArrayList<>();

    private Point structureBlockPos;
    private Block lastOverwrittenBlock;

    private Instance testBuilderInstance;

    private Player player;
    private Instance playerPreviousInstance;
    private Point playerPreviousIntancePos;

    private String name;

    public TestBuilderController(String name) {
        this.name = name;

        testBuilderInstance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        MinecraftServer.getInstanceManager().registerInstance(testBuilderInstance);
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                var point = new Vec(x, 40, z);
                addPositionToBlockCoordLists(point);
                testBuilderInstance.setBlock(point, Block.STONE);
            }
        }
    }

    public void addPlayer(Player player) {
        this.player = player;
        playerPreviousInstance = player.getInstance();
        playerPreviousIntancePos = player.getPosition();
        player.setInstance(testBuilderInstance, new Vec(0, 41, 0));
        System.out.println("tried to set the player instance");
        EventNode<PlayerEvent> testBuilderPlayerEventNode = EventNode.value("Player Block Place:" + player.getDisplayName(), EventFilter.PLAYER, player1 -> player1.getUuid().equals(player.getUuid()));

        testBuilderPlayerEventNode.addListener(PlayerBlockPlaceEvent.class, playerBlockPlaceEvent -> {
            System.out.println("PLAYER BLOCK PLACE EVENT");
            System.out.println("BLOCK POS: " + playerBlockPlaceEvent.getBlockPosition());
            addPositionToBlockCoordLists(playerBlockPlaceEvent.getBlockPosition());
            this.updateStructureOutline();
        });

        testBuilderPlayerEventNode.addListener(PlayerBlockBreakEvent.class, playerBlockBreakEvent -> {
            System.out.println("PLAYER BLOCK BREAK EVENT");
            System.out.println("BLOCK POS: " + playerBlockBreakEvent.getBlockPosition());
            removePositionFromBlockCoordLists(playerBlockBreakEvent.getBlockPosition());
            this.updateStructureOutline();
        });
        MinecraftServer.getGlobalEventHandler().addChild(testBuilderPlayerEventNode);
    }

    public void finish() {
        System.out.println("FINISHING BUILDING STRUCTURE: " + name);
        player.setInstance(playerPreviousInstance, playerPreviousIntancePos);

        Point minPoint = this.getMinPoint();
        Point maxPoint = this.getMaxPoint();

        int sizeX = maxPoint.blockX() - minPoint.blockX() + 1;
        int sizeY = maxPoint.blockY() - minPoint.blockY() + 1;
        int sizeZ = maxPoint.blockZ() - minPoint.blockZ() + 1;

        Structure structure = this.readStructureFromWorld(minPoint, sizeX, sizeY, sizeZ);

        Path root = FileSystems.getDefault().getPath("..").toAbsolutePath();
        Path filePath = Paths.get(root.toString(), "src", "main", "resources", name + ".json");
        StructureWriter structureWriter = new JsonStructureIO();
        structureWriter.writeStructure(structure, filePath);
    }

    private void addPositionToBlockCoordLists(Point point) {
        addCoordToBlockCoordList(point.blockX(), blockXCoords);
        addCoordToBlockCoordList(point.blockY(), blockYCoords);
        addCoordToBlockCoordList(point.blockZ(), blockZCoords);
    }

    private void removePositionFromBlockCoordLists(Point point) {
        removeCoordFromBlockCoordList(point.blockX(), blockXCoords);
        removeCoordFromBlockCoordList(point.blockY(), blockYCoords);
        removeCoordFromBlockCoordList(point.blockZ(), blockZCoords);
    }

    private void updateStructureOutline() {
        Point minPoint = this.getMinPoint();
        Point maxPoint = this.getMaxPoint();

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

    /**
     * Adds the coordinate to the list of integers so that the list stays sorted least to greatest
     *
     * @param coord     The coordinate
     * @param coordList The list of coordinates to insert the coordinate into
     */
    private void addCoordToBlockCoordList(int coord, List<Integer> coordList) {
        int i = 0;
        while (i < coordList.size() && coord > coordList.get(i)) {
            i++;
        }

        // i is now either equal to the size of the coordinate list if coord is greater than every current entry
        // or i is equal to the index of the first entry greater than coord

        if (i < coordList.size()) {
            coordList.add(i, coord);
        } else {
            coordList.add(coord);
        }
    }

    private void removeCoordFromBlockCoordList(int coord, List<Integer> coordList) {
        coordList.remove((Object) coord);
    }

    private Point getMinPoint() {
        return new Vec(blockXCoords.get(0), blockYCoords.get(0), blockZCoords.get(0));
    }

    private Point getMaxPoint() {
        return new Vec(
                blockXCoords.get(blockXCoords.size() - 1),
                blockYCoords.get(blockYCoords.size() - 1),
                blockZCoords.get(blockZCoords.size() - 1));
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
}
