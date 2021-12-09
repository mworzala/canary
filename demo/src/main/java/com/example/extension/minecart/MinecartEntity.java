package com.example.extension.minecart;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.utils.time.TimeUnit;

public class MinecartEntity extends Entity {

    private double speed = 5;

    public MinecartEntity() {
        super(EntityType.ZOMBIE);

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            EntityHeadLookPacket packet = new EntityHeadLookPacket();
            packet.entityId = getEntityId();
            packet.yaw = -90;
            getInstance().sendGroupedPacket(packet);
        }).repeat(1, TimeUnit.SERVER_TICK).schedule();

//        setVelocity(new Vec(10, 0, 0));
    }

    private Vec shapeToDirection(String shape) {
        return switch(shape) {
            case "north_south" -> new Vec(0, 0, 1);
            case "east_west" -> new Vec(1, 0, 0);
            default -> new Vec(0, 0, 0);
        };
    }

    private float shapeToYaw(String shape) {
        return switch(shape) {
            case "north_south" -> 0.0f;
            case "east_west" -> -50.0f;
            default -> throw new RuntimeException("no known shape: " + shape);
        };
    }

    @Override
    public void update(long time) {
        Pos myPos = getPosition();

        Block currentBlock = getInstance().getBlock(myPos);

        if (currentBlock.id() == Block.RAIL.id()) {
            String shape = currentBlock.getProperty("shape");

//            PFPathingEntity

            setVelocity(shapeToDirection(shape).mul(speed));

//            refreshPosition(position.withView(shapeToYaw(shape), 0), false);
        }


    }
}
