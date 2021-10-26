package com.mattworzala.canary.demo;

import com.example.extension.entity.TestEntity;
import com.mattworzala.canary.api.InWorldTest;
import com.mattworzala.canary.api.TestEnvironment;
import net.minestom.server.coordinate.Pos;

public class TestEntityTest {

    @InWorldTest
    public void testWalkToDiamondBlock(TestEnvironment env) {
        final var diamondBlockPos = env.getPos("diamondBlock");
        final var entity = env.spawnEntity(TestEntity::new);

//        env.expect(entity).toBeAt(diamondBlockPos);
    }

    @InWorldTest
    public void testWalkToEntity(TestEnvironment env) {
        System.out.println("TEST WALK TO ENTITY");
        final var entity = env.spawnEntity(TestEntity::new, new Pos(3, 1, 1));
        final var target = env.spawnEntity(TestEntity::new, new Pos(1, 1, 1));

//        env.expect(entity).toBeAt(target::getPosition);
    }
}
