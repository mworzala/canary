package com.mattworzala.canary.server.env;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mattworzala.canary.api.Assertion;
import com.mattworzala.canary.api.TestEnvironment;
import com.mattworzala.canary.platform.util.ClassLoaders;
import com.mattworzala.canary.server.assertion.AssertionImpl;
import com.mattworzala.canary.server.assertion.AssertionResult;
import com.mattworzala.canary.server.givemeahome.Structure;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
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

    public AssertionResult tick() {
        System.out.println("IN TEST ENVIRONMENT TICK");
        boolean failed = false;
        boolean allPassed = true;
        for (var assertion : assertions) {
            var result = assertion.get();
            switch (result) {
                case FAIL -> {
                    failed = true;
                    allPassed = false;
                }
                case NO_RESULT -> allPassed = false;
            }
        }
        // if any test failed, return failed
        if (failed) {
            return AssertionResult.FAIL;
        }
        // if all tests passed, return pass
        if (allPassed) {
            return AssertionResult.PASS;
        }
        // if not all the tests have finished, and nothing has failed, return no result
        return AssertionResult.NO_RESULT;
    }

    public AssertionResult startTesting() {
        System.out.println("STARTING TESTING, there are " + assertions.size() + " assertions");
        EventNode<Event> node = EventNode.all("assertions");
        var handler = MinecraftServer.getGlobalEventHandler();
        CountDownLatch assertionsFinished = new CountDownLatch(assertions.size());
        for (final AssertionImpl<?, ? extends AssertionImpl<?, ?>> assertion : assertions) {
            node.addListener(EventListener.builder(InstanceTickEvent.class)
                    .expireWhen(event -> {
                        if (assertion.get() != AssertionResult.NO_RESULT) {
//                            System.out.println("THING FINISHED");
                            assertionsFinished.countDown();
                            return true;
                        }
                        return false;
                    })
                    .handler((event) -> {

                    }).build());
            handler.addChild(node);
        }

        try {
            assertionsFinished.await();
//            System.out.println("All assertions finished");
            List<String> log = null;
            boolean failed = false;
            for (var assertion : assertions) {
                var result = assertion.get();
                if (result == AssertionResult.FAIL) {
                    log = assertion.getLogs();
                    failed = true;
                }
            }
            // if any test failed, return failed
            if (failed) {
//                System.out.println("TEST FAILED");
                if (log != null) {
                    for (final String logEntry : log) {
                        System.out.println(logEntry);
                    }
                }
                return AssertionResult.FAIL;
            } else {
//                System.out.println("TEST PASSED");
                return AssertionResult.PASS;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return AssertionResult.FAIL;
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
    public Structure loadWorldData(String fileName, int originX, int originY, int originZ) {
        Reader reader = new InputStreamReader(Objects.requireNonNull(ClassLoaders.MINESTOM.getResourceAsStream(fileName)));
        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();

        String id = object.get("id").getAsString();

        var sizeList = new ArrayList<Integer>();
        JsonArray sizeArr = object.get("size").getAsJsonArray();
        for (JsonElement elem : sizeArr) {
            sizeList.add(elem.getAsInt());
        }


        var blockmapArr = object.get("blockmap").getAsJsonArray();
        var blockMap = new HashMap<Integer, Block>();

        blockMap.put(-1, Block.fromNamespaceId("minecraft:air"));

        int index = 0;
        for (JsonElement block : blockmapArr) {
            String blockField;
            String handlerField = "";
            String dataField = "";

            if (block.isJsonObject()) {
                JsonObject blockObj = block.getAsJsonObject();

                blockField = blockObj.get("block").getAsString();

                if (blockObj.get("handler") != null) {
                    handlerField = blockObj.get("handler").getAsString();
                }
                if (blockObj.get("data") != null) {
                    dataField = blockObj.get("data").getAsString();
                }
            } else {
                blockField = block.getAsString();
            }
            var argBlockState = new ArgumentBlockState("blockStateId");
            Block b = argBlockState.parse(blockField);

            if (handlerField.length() > 0) {
                final BlockHandler handler = MinecraftServer.getBlockManager().getHandler(handlerField);
                b = b.withHandler(handler);
            }
            if (dataField.length() > 0) {
                SNBTParser parser = new SNBTParser(new StringReader(dataField));
                try {
                    b = b.withNbt((NBTCompound) parser.parse());
                } catch (NBTException e) {
                    e.printStackTrace();
                }
            }

            blockMap.put(index, b);

            index++;
        }

        var blocks = object.get("blocks").getAsString();
        int sizeX = sizeList.get(0);
        int sizeY = sizeList.get(1);
        int sizeZ = sizeList.get(2);

        int totalBlocks = sizeX * sizeY * sizeZ;

        var blockDefs = blocks.split(";");
        var parsedBlockDefinitions = new ArrayList<BlockDef>(blockDefs.length);
        for (String def : blockDefs) {
            var nums = def.split(",");
            var blockDef = new BlockDef(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]));
            parsedBlockDefinitions.add(blockDef);
        }

        int numBlocksDef = 0;
        for (BlockDef def : parsedBlockDefinitions) {
            numBlocksDef += def.blockCount;
        }
        System.out.println("total number of blocks defined is " + numBlocksDef);
        if (numBlocksDef != totalBlocks) {
            System.out.println(numBlocksDef + " blocks were defined, but the size is " + totalBlocks + " blocks");
            return null;
        }

        Structure resultStructure = new Structure(id, sizeX, sizeY, sizeZ);

        int blockIndex = 0;
        for (BlockDef def : parsedBlockDefinitions) {
            Block block = blockMap.get(def.blockId);
            for (int i = 0; i < def.blockCount; i++) {
                resultStructure.setBlock(blockIndex, block);
                blockIndex++;
            }
        }
        resultStructure.apply(getInstance(), originX, originY, originZ);
        return resultStructure;
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
