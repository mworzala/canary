package com.mattworzala.canary.server.givemeahome;

import com.google.gson.annotations.Expose;
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

    @Expose
    private String id;
    @Expose
    private Vec size;

    @Expose
    Map<Integer, Block> blockmap;

    @Expose
    List<BlockDef> blocks;

    public record BlockDef(int blockId, int blockCount) {
    }


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
        for (BlockDef def : blocks) {
            Block block = blockmap.get(def.blockId);
            for (int i = 0; i < def.blockCount; i++) {
                setBlockInBlockSetter(blockIndex, block, blockSetter);
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
}
