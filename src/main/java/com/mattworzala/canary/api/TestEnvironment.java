package com.mattworzala.canary.api;

import com.mattworzala.canary.api.supplier.EntitySupplier;
import com.mattworzala.canary.api.supplier.LivingEntitySupplier;
import com.mattworzala.canary.api.supplier.PointSupplier;
import com.mattworzala.canary.api.supplier.PosSupplier;
import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.env.TestEnvironmentImpl;
import com.mattworzala.canary.server.structure.Structure;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
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
    <T> T run(String action, Object... args);

    // Assertions

    // Pos
    void expect(PosSupplier actual);
    default void expect(Pos actual) { expect((PosSupplier) () -> actual); }

    // Point/Vec
    void expect(PointSupplier actual);
    default void expect(Point actual) { expect((PointSupplier) () -> actual); }

    // LivingEntity
    <T extends LivingEntity> void expect(LivingEntitySupplier<T> actual);
    default <T extends LivingEntity> void expect(T actual) { expect((LivingEntitySupplier<T>) () -> actual); }

    // Entity
    <T extends Entity> void expect(EntitySupplier<T> actual);
    default <T extends Entity> void expect(T actual) { expect((EntitySupplier<T>) () -> actual); }

//    <T extends Entity> void expect(EntitySupplier<T> actual);
//    <T extends LivingEntity> void expect(Supplier<T> actual);

//    <T extends Entity> EntityAssertion<T> expect(T actual);

//    <T extends LivingEntity> LivingEntityAssertion<T> expect(T actual);

//    <T> Assertion<T> expect(T actual);

    // Instance manipulation utilities

    Structure loadWorldData(String fileName, int originX, int originY, int originZ) throws IOException;

    default <T extends Entity> T spawnEntity(Supplier<T> constructor) {
        return spawnEntity(constructor, Pos.ZERO, null);
    }

    default <T extends Entity> T spawnEntity(Supplier<T> constructor, Pos position) {
        return spawnEntity(constructor, position, null);
    }

    <T extends Entity> T spawnEntity(Supplier<T> constructor, Pos position, Consumer<T> config);

    public static void main(String[] args) {
        LivingEntity li = new LivingEntity();
        TestEnvironment env = new TestEnvironmentImpl();

        env.expect(li);

        env.expect(li::getPosition);
    }
}
