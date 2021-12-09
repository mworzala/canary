package com.mattworzala.canary.demo.minecart;

import com.example.extension.minecart.MinecartEntity;
import com.mattworzala.canary.api.InWorldTest;
import com.mattworzala.canary.api.TestEnvironment;
import net.minestom.server.coordinate.Pos;

public class BasicTests {
    @InWorldTest
    public void straightConstantSpeed(TestEnvironment env) {
        var minecart = env.spawnEntity(MinecartEntity::new, new Pos(1.5, 1, 1.5));
        var starting_minecart_pos = minecart.getPosition();

        env.expect(minecart).toBeAt(starting_minecart_pos.withX(x -> x+2)).and().toHaveYaw(90);
    }

//    @InWorldTest
//    public void straightDontReachEnd(TestEnvironment env) {
//        var minecart = env.spawnEntity(MinecartEntity::new, new Pos(1.5, 1, 1.5));
//        var starting_minecart_pos = minecart.getPosition();
//
//        env.expect(minecart).toBeAt(starting_minecart_pos.withX(x -> x+4));
//        env.expect(minecart).not().toBeAt(starting_minecart_pos.withX(x -> x+3));
//    }


//    @InWorldTest
//    public void fallingBetweenRails(TestEnvironment env) {
//        var minecart = env.spawnEntity(MinecartEntity::new, new Pos(1.5, 3, 1.5));
//        var starting_minecart_pos = minecart.getPosition();
//
//        env.expect(minecart).toBeAt(starting_minecart_pos.withX(x -> x+4).withY(y -> y-2));
//    }

//    @InWorldTest
//    public void sideways(TestEnvironment env) {
//        var minecart = env.spawnEntity(MinecartEntity::new, new Pos(1.5, 1, 1.5));
//        var starting_minecart_pos = minecart.getPosition();
//
//        env.expect(minecart).toBeAt(starting_minecart_pos.withZ(z -> z+3));
//    }
//
//    @InWorldTest
//    public void sideways2(TestEnvironment env) {
//        var minecart = env.spawnEntity(MinecartEntity::new, new Pos(1.5, 1, 1.5));
//        var starting_minecart_pos = minecart.getPosition();
//        var zombie = env.spawnEntity(TestEntity::new, new Pos(0.5, 1, 1.5, 90f, 90f));
//
//        System.out.println(starting_minecart_pos.yaw());
//        env.expect(minecart).toBeAt(starting_minecart_pos.withZ(z -> z+3))
//                .and()
//                .toHaveYaw(0);
//
//    }



//    @ParameterizedInWorldTest
//    @StructureSource("minecart_basic_*")
//    @MethodSource("basicAToBProvider")
//    public void basicAToBTests(TestEnvironment env, Pos expectedPos) {
//        var minecart = env.spawnEntity(MinecartEntity::new, new Pos(1, 1, 1));
//
//        env.expect(minecart).toBeAt(expectedPos);
//    }
//
//    private static Stream<Arguments> basicAToBProvider() {
//        return Stream.of(
//                of(new Pos(3, 1, 1)),
//                of(new Pos(3, 4, 2))
//                // ...
//        );
//    }
}
