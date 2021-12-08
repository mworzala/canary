package com.example.extension.minecart;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

public class MinecartEntity extends Entity {

    public MinecartEntity() {
        super(EntityType.MINECART);
    }

    @Override
    public void tick(long time) {
        setVelocity(new Vec(1, 0, 0));

        super.tick(time);
    }
}
