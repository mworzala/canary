package com.mattworzala.canary.server.env;

import com.mattworzala.canary.api.Assertion;
import com.mattworzala.canary.api.TestEnvironment;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TestEnvironmentImpl implements TestEnvironment {
    private final Instance testInstance;

    public TestEnvironmentImpl(Instance testInstance) {
        this.testInstance = testInstance;
    }

    @Override
    public @NotNull Instance getInstance() {
        return testInstance;
    }

    /*
     * Assertions
     */

    @Override
    public <T extends Entity> Assertion.EntityAssertion<T> expect(T actual) {
        //todo(alex)
        return null;
    }

    @Override
    public <T extends LivingEntity> Assertion.LivingEntityAssertion<T> expect(T actual) {
        //todo(alex)
        return null;
    }

    @Override
    public <T> Assertion<T> expect(T actual) {
        //todo(alex)
        return null;
    }

    /*
     * Structure Variables
     */

    @Override
    public Point getPos(String name) {
        throw new RuntimeException("Saved positions are not currently supported.");
    }

    @Override
    public Block getBlock(String name) {
        throw new RuntimeException("Saved blocks are not currently supported.");
    }

    /*
     * Environment Actions
     */

    @Override
    public <T> T run(String action, Object... args) {
        throw new RuntimeException("Custom environment actions are not currently supported.");
    }

    @Override
    public <T extends Entity> T spawnEntity(Supplier<T> constructor, Pos position, Consumer<T> config) {
        //todo we probably want to track the entity
        T entity = constructor.get();
        if (config != null)
            config.accept(entity);
        entity.setInstance(getInstance(), position);
        return entity;
    }
}
