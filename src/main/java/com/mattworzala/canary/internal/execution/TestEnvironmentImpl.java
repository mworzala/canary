package com.mattworzala.canary.internal.execution;

import com.mattworzala.canary.api.TestEnvironment;
import com.mattworzala.canary.api.supplier.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.mattworzala.canary.api.Assertions.*;

public record TestEnvironmentImpl(TestExecutor executor) implements TestEnvironment {

    static final ThreadLocal<TestEnvironment> CURRENT = new ThreadLocal<>();

    /**
     * @apiNote This <b>must not</b> be accessed from any point other than an {@link com.mattworzala.canary.api.InWorldTest} method.
     *
     * @return The TestEnvironment of the test being evaluated.
     */
    @NotNull
    public static TestEnvironment getActiveEnvironment() {
        TestEnvironment current = CURRENT.get();
        if (current == null)
            throw new IllegalStateException("Cannot access test environment outside of a test method!");
        return current;
    }



    /*
     * Start Impl
     */

    @Override
    public @NotNull Instance getInstance() {
        return executor.getInstance();
    }

    /*
     * Assertions
     */

    // Pos
    @Override
    public PosAssertion expect(PosSupplier actual) {
        return new PosAssertion(actual, executor.createEmptyAssertion());
    }

    // Point/Vec
    @Override
    public PointAssertion expect(PointSupplier actual) {
        return new PointAssertion(actual, executor.createEmptyAssertion());
    }

    // LivingEntity
    @Override
    public LivingEntityAssertion expect(LivingEntitySupplier actual) {
        return new LivingEntityAssertion(actual, executor.createEmptyAssertion());
    }

    // Entity
    @Override
    public EntityAssertion expect(EntitySupplier actual) {
        return new EntityAssertion(actual, executor.createEmptyAssertion());
    }

    // Instance
    @Override
    public InstanceAssertion expect(InstanceSupplier actual) {
        return new InstanceAssertion(actual, executor.createEmptyAssertion());
    }


    /*
     * Structure Variables
     */

    @Override
    public @NotNull Point getPos(String name) {
        throw new RuntimeException("Saved positions are not currently supported.");
    }

    @Override
    public @NotNull Block getBlock(String name) {
        throw new RuntimeException("Saved blocks are not currently supported.");
    }

    /*
     * Environment Actions
     */

    @Override
    public <T extends Entity> T spawnEntity(Supplier<T> constructor, Pos position, Consumer<T> config) {
        //todo we probably want to track the entity
        T entity = constructor.get();
        if (config != null)
            config.accept(entity);
        entity.setInstance(getInstance(), position.add(executor().getOrigin()));
        return entity;
    }
}
