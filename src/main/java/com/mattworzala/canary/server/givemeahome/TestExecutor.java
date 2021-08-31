package com.mattworzala.canary.server.givemeahome;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

// one per test method (reused), handles instantiating the test class, invoking the before/run/after methods, cleaning up the test for the next execution (replace structure).
//   Executing a test is not blocking, it must be ticked until it reports that it has a result.
public class TestExecutor {
    private final TestInstance instance;
    private final Structure structure;
    // The position in the viewer instance. Only used if we are not running in headless mode.
//    private final Point origin; //todo

    public TestExecutor(@NotNull Structure structure) {
        this.instance = new TestInstance();
        this.structure = structure;

        createStructure();
    }

    public void createStructure() {
        // Visual Blocks
        instance.setBlock(0, 41, 0, CanaryBlocks.BoundingBox(structure.size()));

//        instance.setBlock(0, 42, 0, Block.LECTERN);

        //todo place structure

    }

    @NotNull
    public TestInstance getInstance() {
        return instance;
    }
}
