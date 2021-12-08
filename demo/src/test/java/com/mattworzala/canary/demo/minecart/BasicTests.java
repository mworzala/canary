package com.mattworzala.canary.demo.minecart;

import com.example.extension.minecart.MinecartEntity;
import com.mattworzala.canary.api.InWorldTest;
import com.mattworzala.canary.api.TestEnvironment;
import net.minestom.server.coordinate.Pos;

public class BasicTests {
    @InWorldTest
    public void straightConstantSpeed(TestEnvironment env) {
        var minecart = env.spawnEntity(MinecartEntity::new, new Pos(1, 1, 1));

        env.expect(minecart).toBeAt(new Pos(3, 1, 1));
    }
}
