package com.mattworzala.canary.server.env;

import com.mattworzala.canary.api.Assertion;
import com.mattworzala.canary.api.TestEnvironment;
import com.mattworzala.canary.platform.util.ClassLoaders;
import com.mattworzala.canary.server.assertion.AssertionImpl;
import com.mattworzala.canary.server.assertion.AssertionResult;
import com.mattworzala.canary.server.givemeahome.Structure;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import com.mattworzala.canary.server.structure.JsonStructureIO;
import com.mattworzala.canary.server.structure.Structure;
import com.mattworzala.canary.server.structure.StructureReader;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.RelativeBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.SNBTParser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TestEnvironmentImpl implements TestEnvironment {
    private final Instance testInstance;

    record BlockDef(int blockId, int blockCount) {
    }


    private final List<AssertionImpl<?, ?>> assertions = new ArrayList<>();

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
        Assertion.EntityAssertion<T> assertion = new Assertion.EntityAssertion<>(actual);
        assertions.add(assertion);

        return assertion;
    }

    @Override
    public <T extends LivingEntity> Assertion.LivingEntityAssertion<T> expect(T actual) {
        Assertion.LivingEntityAssertion<T> assertion = new Assertion.LivingEntityAssertion<>(actual);
        assertions.add(assertion);

        return assertion;
    }

    @Override
    public <T> Assertion<T> expect(T actual) {
        Assertion<T> assertion = new Assertion<>(actual);
        assertions.add(assertion);

        return assertion;
    }

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
//            List<String> log = null;
//            boolean failed = false;
//            for (var assertion : assertions) {
//                var result = assertion.get();
//                if (result == AssertionResult.FAIL) {
//                    log = assertion.getLogs();
//                    failed = true;
//                }
//            }
//            // if any test failed, return failed
//            if (failed) {
////                System.out.println("TEST FAILED");
//                if (log != null) {
//                    for (final String logEntry : log) {
//                        System.out.println(logEntry);
//                    }
//                }
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
    public Structure loadWorldData(String fileName, int originX, int originY, int originZ) {
        var resource = ClassLoaders.MINESTOM.getResource(fileName);
        if (resource == null) {

        return null;
        }
        try {
            var uri = resource.toURI();
            Path p = Paths.get(uri);
            StructureReader structureReader = new JsonStructureIO();
            var structure = structureReader.readStructure(p);

            RelativeBlockBatch blockBatch = new RelativeBlockBatch();
            structure.loadIntoBlockSetter(blockBatch);
            blockBatch.apply(getInstance(), originX, originY, originZ, () -> System.out.println("Applied the structure to the world!"));

            return structure;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
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
