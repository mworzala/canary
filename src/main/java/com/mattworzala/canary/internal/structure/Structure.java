package com.mattworzala.canary.internal.structure;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Structure(String id, int sizeX, int sizeY, int sizeZ) {
        this.id = id;
        size = new Vec(sizeX, sizeY, sizeZ);

        this.blockmap = new HashMap<>();
        this.blocks = new ArrayList<>();
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

    /**
     * Index assumes an XZY packing order
     *
     * @param index
     * @param block
     * @param blockSetter
     */
    private void setBlockInBlockSetter(int index, @NotNull Block block, Block.Setter blockSetter, Point offset) {
        int x = index % this.getSizeX();
        int z = index % (this.getSizeX() * this.getSizeZ()) / this.getSizeX();
        int y = index / (this.getSizeX() * this.getSizeZ());
        assert x <= this.getSizeX();
        assert y <= this.getSizeY();
        assert z <= this.getSizeZ();

        blockSetter.setBlock(offset.add(x, y, z), block);
    }

    public void loadIntoBlockSetter(Block.Setter blockSetter, Point offset) {
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
