package com.mattworzala.canary.server.env;

import com.mattworzala.canary.api.Assertion;
import com.mattworzala.canary.api.TestEnvironment;
import com.mattworzala.canary.server.assertion.AssertionImpl;
import com.mattworzala.canary.server.assertion.AssertionResult;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TestEnvironmentImpl implements TestEnvironment {
    private final Instance testInstance;

    private List<AssertionImpl> assertions = new ArrayList<>();
    private List<Object> assertionInput = new ArrayList<>();

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
        Assertion.EntityAssertion<T> assertion = new Assertion.EntityAssertion<>();
        assertions.add(assertion);
        assertionInput.add(actual);

        return assertion;
    }

    @Override
    public <T extends LivingEntity> Assertion.LivingEntityAssertion<T> expect(T actual) {
        Assertion.LivingEntityAssertion<T> assertion = new Assertion.LivingEntityAssertion<>();
        assertions.add(assertion);
        assertionInput.add(actual);

        return assertion;
    }

    @Override
    public <T> Assertion<T> expect(T actual) {
        Assertion assertion = new Assertion();
        assertions.add(assertion);
        assertionInput.add(actual);

        return assertion;
    }

    public void startTesting() {
        System.out.println("STARTING TESTING, there are " + assertions.size() + " assertions");
        EventNode<Event> node = EventNode.all("assertions");
        var handler = MinecraftServer.getGlobalEventHandler();
        for(var i = 0; i < assertions.size(); i++) {
            final int index = i;
            final var assertion = assertions.get(i);
            final var input = assertionInput.get(i);

            node.addListener(EventListener.builder(InstanceTickEvent.class)
                    .expireCount(100)
                    .handler((event) -> {
                        System.out.println("INSTANCE TICK EVENT");
                        var result = assertion.apply(input);
                        switch (result) {
                            case PASS -> System.out.println("test " + index + " passed");
                            case FAIL -> System.out.println("test " + index + " failed");
                            case NO_RESULT -> System.out.println("test " + index + " had no result");
                        }
                    }).build());
        }
        handler.addChild(node);
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
