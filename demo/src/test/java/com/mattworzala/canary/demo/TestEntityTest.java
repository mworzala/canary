package com.mattworzala.canary.demo;

import com.example.extension.entity.TestEntity;
import com.mattworzala.canary.TestEnvironment;
import com.mattworzala.canary.test.InWorldTest;

public class TestEntityTest {
    @InWorldTest
    public void testWalkToDiamondBlock(TestEnvironment env) {
        final var diamondBlockPos = env.getPos("diamondBlock");
        final var entity = env.spawnEntity(TestEntity::new);

        env.passWhenEntityPresent(entity, diamondBlockPos);
    }
}
