/*
From Minestom VanillaReimplementation beta 1.17 update
https://github.com/Minestom/VanillaReimplementation/blob/1.17.1-update/src/main/java/net/minestom/vanilla/entity/FallingBlockEntity.java
 */

package net.minestom.vanilla.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Random;

public class FallingBlockEntity extends Entity {
    private static final Random rng = new Random();
    private final Block toPlace;

    public FallingBlockEntity(Block toPlace, Point initialPosition) {
        super(EntityType.FALLING_BLOCK);
        this.toPlace = toPlace;

        // setGravity(0.025f, getGravityAcceleration());
        setBoundingBox(0.98f, 0.98f, 0.98f);

        FallingBlockMeta meta = (FallingBlockMeta) this.getEntityMeta();
        meta.setBlock(toPlace);
        meta.setSpawnPosition(initialPosition);
    }

    @Override
    public void update(long time) {
        // TODO: Cleanup this method structure

        if (!isOnGround()) {
            return;
        }

        if (getVelocity().y() < 0.0) {
            return;
        }

        Block block = instance.getBlock(position);
        Block belowBlock = instance.getBlock(position.add(0, -1, 0));

//        if ((!block.isAir() && !block.isLiquid()) || !belowBlock.isSolid()) {
//             TODO: Better way to get block's loot
//            Material loot = Material.fromNamespaceId(toPlace.namespace());
//
//            ItemEntity entity = new ItemEntity(ItemStack.of(loot));
//            entity.setInstance(instance);
//            entity.teleport(position);
//            remove();
//            return;
//        }

        instance.setBlock(position, toPlace);
        remove();
    }
}
