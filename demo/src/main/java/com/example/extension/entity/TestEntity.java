package com.example.extension.entity;

import com.example.extension.entity.ai.goal.StandOnBlockGoal;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.instance.block.Block;

public class TestEntity extends EntityCreature {

    public TestEntity() {
        super(EntityType.ZOMBIE);

        addAIGroup(
                new EntityAIGroupBuilder()
                        .addGoalSelector(new StandOnBlockGoal(this, Block.DIAMOND_BLOCK))
                        .build()
        );
    }
}
