package com.mattworzala.canary.internal.execution.tracker;

import net.minestom.server.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EntityTracker implements Tracker<Entity> {
    private static final Logger logger = LoggerFactory.getLogger(EntityTracker.class);

    private final List<Entity> entities = new ArrayList<>();

    @Override
    public boolean canTrack(Object thing) {
        return thing instanceof Entity;
    }

    @Override
    public void track(Entity entity) {
        logger.info("Tracking {}", entity);
        entities.add(entity);
    }

    @Override
    public void release() {
        entities.forEach(ent -> {
            logger.info("Releasing {}", ent);
            ent.remove();
        });
        entities.clear();
    }
}
