package com.mattworzala.canary.api;

import com.mattworzala.canary.api.supplier.*;
import com.mattworzala.canary.internal.util.safety.EnvType;
import com.mattworzala.canary.internal.util.safety.Env;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.mattworzala.canary.api.Assertions.*;

@Env(EnvType.MINESTOM)
public interface TestEnvironment {

    @NotNull Instance getInstance();

    @NotNull Point getPos(String name);

    @NotNull Block getBlock(String name);

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
//    <T> T run(String action, Object... args);

    // Assertions
    // @formatter:off

    // Pos
    PosAssertion expect(PosSupplier actual);
    default PosAssertion expect(Pos actual) { return expect(() -> actual); }

    // Point/Vec
    PointAssertion expect(PointSupplier actual);
    default PointAssertion expect(Point actual) { return expect(() -> actual); }

    // LivingEntity
    LivingEntityAssertion expect(LivingEntitySupplier actual);
    default LivingEntityAssertion expect(LivingEntity actual) { return expect(() -> actual); }

    // Entity
    EntityAssertion expect(EntitySupplier actual);
    default EntityAssertion expect(Entity actual) { return expect(() -> actual); }

    // Instance
    InstanceAssertion expect(InstanceSupplier actual);
    default InstanceAssertion expect(Instance actual) { return expect(() -> actual); }

    // @formatter:on

    // Instance manipulation utilities

    default <T extends Entity> T spawnEntity(Supplier<T> constructor) {
        return spawnEntity(constructor, Pos.ZERO, null);
    }

    default <T extends Entity> T spawnEntity(Supplier<T> constructor, Pos position) {
        return spawnEntity(constructor, position, null);
    }

    <T extends Entity> T spawnEntity(Supplier<T> constructor, Pos position, Consumer<T> config);
}
