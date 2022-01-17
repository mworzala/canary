package com.mattworzala.canary.demo;

import com.example.extension.entity.TestEntity;
import com.mattworzala.canary.api.TestEnvironment;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

import static com.mattworzala.canary.api.Expect.expect;

public class TestEntityTest {

//    @InWorldTest
    public void testWalkToDiamondBlock(TestEnvironment env) {
        final var diamondBlockPos = new Vec(2, 1, 3);
        final var entity = env.spawnEntity(TestEntity::new, new Pos(2, 1, 1));

        expect(entity).toBeAt(diamondBlockPos);
    }

//    @InWorldTest
    public void testWalkToEntity(TestEnvironment env) {
        final var entity = env.spawnEntity(TestEntity::new, new Pos(3, 1, 1));
        final var target = env.spawnEntity(TestEntity::new, new Pos(1, 1, 1));

        expect(entity).toBeAt(target::getPosition);
    }
}
