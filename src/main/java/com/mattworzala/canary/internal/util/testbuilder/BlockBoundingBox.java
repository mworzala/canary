package com.mattworzala.canary.internal.util.testbuilder;

import com.mattworzala.canary.internal.util.point.PointUtil;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

/**
 * A class that lets you add and remove block to it, keeping track of the bounding box around those points
 * Used in TestBuilderController for handling the structure block bounding box
 */
public class BlockBoundingBox {

    private final int MAX_DIMENSION;


    // (smallest x, smallest y, smallest z)
    private Point minPoint;

    // (largest x, largest y, largest z)
    private Point maxPoint;

    private final int[] xBlockCounts;
    private final int[] yBlockCounts;
    private final int[] zBlockCounts;

    public BlockBoundingBox(int MAX_DIMENSION) {
        this.MAX_DIMENSION = MAX_DIMENSION;
        xBlockCounts = new int[MAX_DIMENSION];
        yBlockCounts = new int[MAX_DIMENSION];
        zBlockCounts = new int[MAX_DIMENSION];
    }

    /**
     * Adds a block at the given point to the bounding box
     * if the point is somewhere that would make the bounding box too large, doesn't add the point, returns false
     * otherwise returns true
     *
     * @param p Coordinates of the block added
     * @return True if the block was successfully added and bounded, false if not
     */
    public boolean addBlock(Point p) {
        // if we haven't gotten any blocks, set up around this point
        if (minPoint == null) {
            handleFirstPoint(p);
            return true;
        }

        Point newMin = PointUtil.minOfPoints(minPoint, p);
        Point newMax = PointUtil.maxOfPoints(maxPoint, p);
        Point newSize = newMax.sub(newMin);
        // if the new block is in a position that cannot be encapsulated by a structure bounding box, don't insert
        if (newSize.blockX() > MAX_DIMENSION || newSize.blockZ() > MAX_DIMENSION || newSize.blockY() > MAX_DIMENSION) {
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
        Point offset = p.sub(minPoint);
        xBlockCounts[offset.blockX()] += 1;
        yBlockCounts[offset.blockY()] += 1;
        zBlockCounts[offset.blockZ()] += 1;
        return true;
    }

    private void handleFirstPoint(Point p) {
        minPoint = p;
        maxPoint = p;
        xBlockCounts[0] = 1;
        yBlockCounts[0] = 1;
        zBlockCounts[0] = 1;
    }

    /**
     * Removes the block at the given position, the bounding box changes accordingly
     *
     * @param p Point of the block removed
     */
    public void removeBlock(Point p) {
        Point offset = p.sub(minPoint);
        xBlockCounts[offset.blockX()] -= 1;
        yBlockCounts[offset.blockY()] -= 1;
        zBlockCounts[offset.blockZ()] -= 1;

        // we want the 0th index of every block counts array to actually blocks there
        int deltaX = firstNonZero(xBlockCounts);
        shiftArray(xBlockCounts, -deltaX);
        int deltaY = firstNonZero(yBlockCounts);
        shiftArray(yBlockCounts, -deltaY);
        int deltaZ = firstNonZero(zBlockCounts);
        shiftArray(zBlockCounts, -deltaZ);

        minPoint = minPoint.add(new Vec(deltaX, deltaY, deltaZ));

        recomputeMaxPoint();
    }


    private void recomputeMaxPoint() {
        Point offset = maxPoint.sub(minPoint);
        int x = getFirstNonZeroBeforeIndex(xBlockCounts, offset.blockX());
        int y = getFirstNonZeroBeforeIndex(yBlockCounts, offset.blockY());
        int z = getFirstNonZeroBeforeIndex(zBlockCounts, offset.blockZ());
        maxPoint = minPoint.add(new Vec(x, y, z));
    }

    /**
     * Returns the index of the first non-zero value in arr before index
     *
     * @param arr
     * @param index
     * @return
     */
    private int getFirstNonZeroBeforeIndex(int[] arr, int index) {
        for (int i = index; i >= 0; i--) {
            if (arr[i] != 0) {
                return i;
            }
        }
        return 0;
    }

    private int firstNonZero(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0) {
                return i;
            }
        }
        return arr.length;
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


    /**
     * @return The point with the smallest x, y, and z, value bounded
     */
    public Point getMinPoint() {
        return minPoint;
    }

    /**
     * @return The point with the largest x, y, and z, value bounded
     */
    public Point getMaxPoint() {
        return maxPoint;
    }


    /**
     * The number of blocks bounded in each axis
     * A bounding box around a single block has the same minPoint and maxPoint, but a size of (1, 1, 1)
     *
     * @return
     */
    public Point getSize() {
        return maxPoint.sub(minPoint).add(PointUtil.UNIT_VECTOR);
    }
}
