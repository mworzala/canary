package com.mattworzala.canary.demo;

import com.example.extension.entity.TestEntity;
import com.mattworzala.canary.api.TestEnvironment;

public class TestEntityTest {

    public void testWalkToDiamondBlock(TestEnvironment env) {
        final var diamondBlockPos = env.getPos("diamondBlock");
        final var entity = env.spawnEntity(TestEntity::new);

        env.expect(entity).toBeAt(diamondBlockPos);
    }
}
