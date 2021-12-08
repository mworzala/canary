package com.mattworzala.canary.internal.structure;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockGetter;
import net.minestom.server.instance.block.BlockSetter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This will be replaced eventually
 * <p>
 * todo(alex)
 */
public class Structure {

    public record BlockDef(int blockId, int blockCount) {
    }

    private String id;
    private Vec size;

    Map<Integer, Block> blockmap;

    List<BlockDef> blocks;

    private Map<String, Integer> markers;

    public Structure(String id, int sizeX, int sizeY, int sizeZ) {
        this.id = id;
        size = new Vec(sizeX, sizeY, sizeZ);

        this.blockmap = new HashMap<>();
        this.blocks = new ArrayList<>();
        this.markers = new HashMap<>();
    }

    public void addMarker(String name, int index) {
        this.markers.put(name, index);
    }

    public void addMarker(String name, Point point) {
        this.markers.put(name, pointToIndex(point));
    }

    public void putInBlockMap(int index, Block block) {
        blockmap.put(index, block);
    }

    public void addToBlockDefList(BlockDef blockDef) {
        blocks.add(blockDef);
    }

    public void setBlocks(List<BlockDef> blockDefs) {
        this.blocks = blockDefs;
    }

    public List<BlockDef> getBlocks() {
        return this.blocks;
    }

    public Vec getSize() {
        return this.size;
    }

    public int getSizeX() {
        return this.size.blockX();
    }

    public int getSizeY() {
        return this.size.blockY();
    }

    public int getSizeZ() {
        return this.size.blockZ();
    }

    public Point indexToPoint(int index) {
        int x = index % this.getSizeX();
        int y = index / (this.getSizeX() * this.getSizeZ());
        int z = index % (this.getSizeX() * this.getSizeZ()) / this.getSizeX();

        boolean inBounds = x <= this.getSizeX();
        inBounds = inBounds || y <= this.getSizeY();
        inBounds = inBounds || z <= this.getSizeZ();

        if (inBounds) {
            return new Vec(x, y, z);
        } else {
            return null;
        }
    }

    public int pointToIndex(Point p) {
        return (p.blockY() * this.getSizeX() * this.getSizeZ()) + (p.blockZ() * this.getSizeX()) + p.blockX();
    }

    /**
     * Index assumes an XZY packing order
     *
     * @param index
     * @param block
     * @param blockSetter
     */
    private void setBlockInBlockSetter(int index, @NotNull Block block, BlockSetter blockSetter, Point offset) {
        Point p = this.indexToPoint(index);
        if (p != null) {
            blockSetter.setBlock(offset.add(p), block);
        }
    }

    public void loadIntoBlockSetter(BlockSetter blockSetter, Point offset) {
        int blockIndex = 0;
        for (BlockDef def : blocks) {
            Block block = blockmap.get(def.blockId);
            for (int i = 0; i < def.blockCount; i++) {
                setBlockInBlockSetter(blockIndex, block, blockSetter, offset);
                blockIndex++;
            }
        }
    }

    public String getId() {
        return id;
    }

    public Map<Integer, Block> getBlockMap() {
        return blockmap;
    }

    public Map<String, Integer> getMarkers() {
        return markers;
    }

    public Map<String, Point> getMarkersAsPoints() {
        Map<String, Point> pointMarkers = new HashMap<>();
        getMarkers().forEach((k, v) -> {
            pointMarkers.put(k, indexToPoint(v));
        });
        return pointMarkers;
    }


    public static Structure structureFromWorld(Instance instance, String id, Point origin, Point size) {
        return structureFromWorld(instance, id, origin, size.blockX(), size.blockY(), size.blockZ());
    }

    public static Structure structureFromWorld(Instance instance, String id, Point origin, int sizeX, int sizeY, int sizeZ) {
        Structure resultStructure = new Structure(id, sizeX, sizeY, sizeZ);

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
                    Block b = instance.getBlock(origin.blockX() + x, origin.blockY() + y, origin.blockZ() + z, BlockGetter.Condition.NONE);
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id: ");
        stringBuilder.append(this.getId());
        stringBuilder.append("\n");

        stringBuilder.append("size: ");
        stringBuilder.append(String.format("%d, %d, %d", this.getSizeX(), this.getSizeY(), this.getSizeZ()));
        stringBuilder.append("\n");

        stringBuilder.append("block map:\n");
        var index = -1;
        while (this.blockmap.containsKey(index)) {
            stringBuilder.append(index + ": " + this.blockmap.get(index));
            stringBuilder.append("\n");
            index++;
        }

        stringBuilder.append("block defs:\n");
        for (var blockDef : this.blocks) {
            stringBuilder.append(blockDef);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
