package com.mattworzala.canary.api;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.structure.Structure;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.mattworzala.canary.api.Assertion.EntityAssertion;
import static com.mattworzala.canary.api.Assertion.LivingEntityAssertion;

@Environment(EnvType.MINESTOM)
public interface TestEnvironment {
    @NotNull Instance getInstance();


    Point getPos(String name);

    Block getBlock(String name);


    /*
        todo could be a way to handle user defined actions, eg

        (somewhere)
        @EnvironmentAction("press_button")
        public void pressButtonAction(TestEnvironment env, Point position) {
            // somehow press a button at `position`
        }

        (somewhere else in a test)
        env.run("press_button", buttonPos);
     */
    <T> T run(String action, Object... args);


    // Assertions

    <T extends Entity> EntityAssertion<T> expect(T actual);

    <T extends LivingEntity> LivingEntityAssertion<T> expect(T actual);

    <T> Assertion<T> expect(T actual);

//    AssertionResult tick();

//    AssertionResult startTesting();

    // Instance manipulation utilities

    Structure loadWorldData(String fileName, int originX, int originY, int originZ) throws IOException;

    default <T extends Entity> T spawnEntity(Supplier<T> constructor) {
        return spawnEntity(constructor, Pos.ZERO, null);
    }

    default <T extends Entity> T spawnEntity(Supplier<T> constructor, Pos position) {
        return spawnEntity(constructor, position, null);
    }

    <T extends Entity> T spawnEntity(Supplier<T> constructor, Pos position, Consumer<T> config);
}
