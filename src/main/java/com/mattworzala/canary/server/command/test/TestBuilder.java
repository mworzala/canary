package com.mattworzala.canary.server.command.test;

import com.mattworzala.canary.server.instance.block.BoundingBoxHandler;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;

import java.util.function.Consumer;

/**
 * This class represents the interface through which the player can make tests
 */
public class TestBuilder {

    private Player player;
    private boolean doneBuilding = false;

    private int pointsSelected;

    private Point minPoint;
    private Point maxPoint;
    private static Point ONE_BY_ONE_BY_ONE_POINT = new Vec(1, 1, 1);

    private Consumer<String> messageCallback;

    public TestBuilder(Player player, Consumer<String> messageCallback) {
        this.player = player;
        this.messageCallback = messageCallback;
    }

    public void handleTestBuilderSelect(Point pointClicked) {

//        messageCallback.accept("points clicked: " + this.pointsSelected);
        if (pointsSelected == 0) {
            minPoint = pointClicked;
            maxPoint = pointClicked;
        } else {
            minPoint = minOfPoints(minPoint, pointClicked);
            maxPoint = maxOfPoints(maxPoint, pointClicked);
        }


        this.pointsSelected++;
        this.drawBoundingBox();

        // if we have a non-zero x, y and z size, then we are done
        Point size = maxPoint.sub(minPoint);
        if (size.blockX() != 0 &&
                size.blockY() != 0 &&
                size.blockZ() != 0) {
            this.doneBuilding = true;
        }
    }

    private void drawBoundingBox() {
        var boundingBox = BoundingBoxHandler.BLOCK
                .withTag(BoundingBoxHandler.Tags.SizeX, this.getSize().blockX())
                .withTag(BoundingBoxHandler.Tags.SizeY, this.getSize().blockY())
                .withTag(BoundingBoxHandler.Tags.SizeZ, this.getSize().blockZ());
        BlockEntityDataPacket blockEntityDataPacket = new BlockEntityDataPacket();

        Point blockPos = this.getOrigin().add(new Vec(0, -1, 0));
        blockEntityDataPacket.blockPosition = blockPos;
        blockEntityDataPacket.action = 7;
        blockEntityDataPacket.nbtCompound = boundingBox.nbt();

        player.getInstance().setBlock(blockPos, boundingBox);
        player.sendPacketToViewersAndSelf(blockEntityDataPacket);
    }

    private Point minOfPoints(Point p1, Point p2) {
        return new Vec(Math.min(p1.x(), p2.x()), Math.min(p1.y(), p2.y()), Math.min(p1.z(), p2.z()));
    }

    private Point maxOfPoints(Point p1, Point p2) {
        return new Vec(Math.max(p1.x(), p2.x()), Math.max(p1.y(), p2.y()), Math.max(p1.z(), p2.z()));
    }

    public Boolean isDoneBuilding() {
        return doneBuilding;
    }

    public Point getOrigin() {
        return minPoint;
    }

    public Point getSize() {
        // size is the number of blocks, not the coordinate distance, so we need to add one
        // fencepost error
        return maxPoint.sub(minPoint).add(ONE_BY_ONE_BY_ONE_POINT);
    }
}
