package com.mattworzala.canary.demo;

import com.example.extension.entity.TestEntity;
import com.mattworzala.canary.api.InWorldTest;
import com.mattworzala.canary.api.TestEnvironment;
import net.minestom.server.coordinate.Pos;

public class TestEntityTest {

    public void testWalkToDiamondBlock(TestEnvironment env) {
        final var diamondBlockPos = env.getPos("diamondBlock");
        final var entity = env.spawnEntity(TestEntity::new);

        env.expect(entity).toBeAt(diamondBlockPos);
    }

    @InWorldTest
    public void testWalkToEntity(TestEnvironment env) {
        final var entity = env.spawnEntity(TestEntity::new, new Pos(0, 41, 0));
        final var target = env.spawnEntity(TestEntity::new, new Pos(2, 41, 0));

//        env.expect(entity).toBeAt(target::getPosition);
    }
}
