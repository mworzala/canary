package com.example.extension.minecart;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

public class BrokeEntity extends Entity {

    long totalTime = 0;

    public BrokeEntity() {
        super(EntityType.ZOMBIE);
    }

    @Override
    public void update(long time) {
        if (totalTime == 0) {
            totalTime = time;
        }

        if (time - totalTime < 2000) {
            position = position.withView(-90, 0);
//            setView(0, 0);
            setVelocity(new Vec(0, 0, 5));
            System.out.println(time);
        }

    }
}
