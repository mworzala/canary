package com.mattworzala.canary.server.givemeahome;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockSetter;
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

    private String id;
    private Vec size;

    Map<Integer, Block> blockMap;

    public record BlockDef(int blockId, int blockCount) {
    }

    List<BlockDef> blockDefList;

    public Structure(String id, int sizeX, int sizeY, int sizeZ) {
        this.id = id;
        size = new Vec(sizeX, sizeY, sizeZ);

        this.blockMap = new HashMap<>();
        this.blockDefList = new ArrayList<>();
    }

    public void putInBlockMap(int index, Block block) {
        blockMap.put(index, block);
    }

    public void addToBlockDefList(BlockDef blockDef) {
        blockDefList.add(blockDef);
    }

    public void setBlockDefList(List<BlockDef> blockDefs) {
        this.blockDefList = blockDefs;
    }

    public Vec getSize() {
        return this.size;
    }

    public int getSizeX() {
        return (int) this.size.x();
    }

    public int getSizeY() {
        return (int) this.size.y();
    }

    public int getSizeZ() {
        return (int) this.size.z();
    }

    /**
     * Index assumes an XZY packing order
     *
     * @param index
     * @param block
     * @param blockSetter
     */
    private void setBlockInBlockSetter(int index, @NotNull Block block, BlockSetter blockSetter) {
        int x = index % this.getSizeX();
        int z = index % (this.getSizeX() * this.getSizeZ()) / this.getSizeZ();
        int y = index / (this.getSizeX() * this.getSizeZ());
        assert x <= this.getSizeX();
        assert y <= this.getSizeY();
        assert z <= this.getSizeZ();

        blockSetter.setBlock(x, y, z, block);
    }

    public void loadIntoBlockSetter(BlockSetter blockSetter) {
        int blockIndex = 0;
        for (BlockDef def : blockDefList) {
            Block block = blockMap.get(def.blockId);
            for (int i = 0; i < def.blockCount; i++) {
                System.out.println("block index: " + blockIndex);
                System.out.println("block: " + block);
                setBlockInBlockSetter(blockIndex, block, blockSetter);
                blockIndex++;
            }
        }
    }
}
