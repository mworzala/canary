package com.mattworzala.canary.demo.another;

import com.example.extension.entity.TestEntity;
import com.mattworzala.canary.api.InWorldTest;
import com.mattworzala.canary.api.TestEnvironment;
import net.minestom.server.coordinate.Pos;

public class TestDemo {
//    @InWorldTest
    public void anotherTest(TestEnvironment env) {
        var entity = env.spawnEntity(TestEntity::new, new Pos(1, 1, 1));

        env.expect(entity).toBeAt(new Pos(3, 1, 1));
    }
}
