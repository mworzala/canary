package com.mattworzala.canary.demo;

import com.example.extension.entity.TestEntity;
import com.mattworzala.canary.api.InWorldTest;
import com.mattworzala.canary.api.TestEnvironment;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

public class TestEntityTest {

    public void testWalkToDiamondBlock(TestEnvironment env) {
        final var diamondBlockPos = env.getPos("diamondBlock");
        final var entity = env.spawnEntity(TestEntity::new);

        env.expect(entity).toBeAt(diamondBlockPos);
    }

    @InWorldTest
    public void testWalkToEntity(TestEnvironment env) {
        System.out.println("TEST WALK TO ENTITY");
        final var entity = env.spawnEntity(TestEntity::new, new Pos(0, 43, 0));
        final var target = env.spawnEntity(TestEntity::new, new Pos(2, 43, 0));

        // Create a diamond block for them to walk to
        env.getInstance().setBlock(0, 40, 3, Block.DIAMOND_BLOCK);

        env.expect(entity).toBeAt(target::getPosition);
    }
}
