package com.mattworzala.canary.internal.execution.env;

import com.mattworzala.canary.api.TestEnvironment;
import com.mattworzala.canary.api.supplier.EntitySupplier;
import com.mattworzala.canary.api.supplier.LivingEntitySupplier;
import com.mattworzala.canary.internal.assertion.AssertionStep;
import com.mattworzala.canary.internal.execution.TestExecutor;
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

public record TestEnvironmentImpl(TestExecutor executor) implements TestEnvironment {

    @Override
    public @NotNull Instance getInstance() {
        return executor.getInstance();
    }

    /*
     * Assertions
     */

//    // Pos
//    @Override
//    public PosSupplier get(Pos actual) {
//        return null; //todo
//    }
//
//    @Override
//    public PosAssertion expect(PosSupplier actual) {
//        return reg(new PosAssertion(actual));
//    }
//
//    // Point/Vec
//    @Override
//    public PointSupplier get(Point actual) {
//        return null; //todo
//    }
//
//    @Override
//    public PointAssertion expect(PointSupplier actual) {
//        return reg(new PointAssertion(actual));
//    }

    // LivingEntity
    @Override
    public LivingEntityAssertion expect(LivingEntitySupplier actual) {
        return new LivingEntityAssertion(actual, executor.createEmptyAssertion());
    }

    // Entity
    @Override
    public EntitySupplier get(Entity actual) {
        return null; //todo
    }

    @Override
    public EntityAssertion expect(EntitySupplier actual) {
        return null;
//        return new EntityAssertion(actual, newAssertion(actual));
    }

//    // Instance
//    @Override
//    public InstanceSupplier get(Instance actual) {
//        return null; //todo
//    }
//
//    @Override
//    public InstanceAssertion expect(InstanceSupplier actual) {
//        return reg(new InstanceAssertion(actual));
//    }

    //    public AssertionResult tick() {
//        System.out.println("IN TEST ENVIRONMENT TICK");
//        boolean failed = false;
//        boolean allPassed = true;
//        for (var assertion : assertions) {
//            var result = assertion.get();
//            switch (result) {
//                case FAIL -> {
//                    failed = true;
//                    allPassed = false;
//                }
//                case NO_RESULT -> allPassed = false;
//            }
//        }
//        // if any test failed, return failed
//        if (failed) {
//            return AssertionResult.FAIL;
//        }
//        // if all tests passed, return pass
//        if (allPassed) {
//            return AssertionResult.PASS;
//        }
//        // if not all the tests have finished, and nothing has failed, return no result
//        return AssertionResult.NO_RESULT;
//    }

//    public AssertionResult startTesting() {
//        System.out.println("STARTING TESTING, there are " + assertions.size() + " assertions");
//        EventNode<Event> node = EventNode.all("assertions");
//        var handler = MinecraftServer.getGlobalEventHandler();
//        CountDownLatch assertionsFinished = new CountDownLatch(assertions.size());
//        for (final AssertionImpl<?, ? extends AssertionImpl<?, ?>> assertion : assertions) {
//            node.addListener(EventListener.builder(InstanceTickEvent.class)
//                    .expireWhen(event -> {
//                        if (assertion.get() != AssertionResult.NO_RESULT) {
////                            System.out.println("THING FINISHED");
//                            assertionsFinished.countDown();
//                            return true;
//                        }
//                        return false;
//                    })
//                    .handler((event) -> {
//
//                    }).build());
//            handler.addChild(node);
//        }
//
//        try {
//            assertionsFinished.await();
////            System.out.println("All assertions finished");
//            boolean failed = false;
//            for (var assertion : assertions) {
//                var result = assertion.get();
//                if (result == AssertionResult.FAIL) {
//                    failed = true;
//                }
//            }
//            // if any test failed, return failed
//            if (failed) {
////                System.out.println("TEST FAILED");
//                return AssertionResult.FAIL;
//            } else {
////                System.out.println("TEST PASSED");
//                return AssertionResult.PASS;
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return AssertionResult.FAIL;
//    }

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
    public <T> T run(String action, Object... args) {
        throw new RuntimeException("Custom environment actions are not currently supported.");
    }

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
