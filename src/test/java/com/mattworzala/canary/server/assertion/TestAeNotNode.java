package com.mattworzala.canary.server.assertion;

import com.mattworzala.canary.server.assertion.node.AeNotNode;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAeNotNode {

    @Test
    public void testChildPass() {
        AeNotNode node = new AeNotNode(List.of(AeTestNode.PASSED));

        assertEquals(Result.FAILED, node.evaluate(null));
    }

    @Test
    public void testChildFail() {
        AeNotNode node = new AeNotNode(List.of(AeTestNode.FAILED));

        assertEquals(Result.PASSED, node.evaluate(null));

        assertEquals(Block.ANDESITE, Block.ANDESITE);
    }
}
