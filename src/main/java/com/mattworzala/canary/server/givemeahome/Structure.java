package com.mattworzala.canary.server.givemeahome;

import com.extollit.linalg.mutable.Vec3i;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.RelativeBlockBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * This will be replaced eventually
 * <p>
 * todo(alex)
 */
public class Structure {

    private String id;
    private Vec3i size;
    private RelativeBlockBatch blockBatch;

    public Structure(String id, int sizeX, int sizeY, int sizeZ) {
        this.id = id;
        size = new Vec3i(sizeX, sizeY, sizeZ);
        blockBatch = new RelativeBlockBatch();
    }

    public Vec3i getSize() {
        return this.size;
    }

    public int getSizeX() {
        return this.size.x;
    }

    public int getSizeY() {
        return this.size.y;
    }

    public int getSizeZ() {
        return this.size.z;
    }

    /**
     * Index assumes an XZY packing order
     *
     * @param index
     * @param block
     */
    public void setBlock(int index, @NotNull Block block) {
        int x = index % this.size.x;
        int z = index % (this.size.x * this.size.z) / this.size.z;
        int y = index / (this.size.x * this.size.z);
        assert x <= this.size.x;
        assert y <= this.size.y;
        assert z <= this.size.z;
        this.setBlock(x, y, z, block);
    }

    public void setBlock(int x, int y, int z, @NotNull Block block) {
        blockBatch.setBlock(x, y, z, block);
    }

    public void apply(Instance instance, int originX, int originY, int originZ) {
        blockBatch.apply(instance, originX, originY, originZ, () -> System.out.println("STRUCTURE WITH ID: " + this.id + " APPLIED!"));
    }
}
