package com.mattworzala.canary.internal.assertion;

import com.mattworzala.canary.internal.assertion.node.AeNotNode;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AeNotNodeTest {

    @Test
    public void testChildPass() {
        AeNotNode node = new AeNotNode(List.of(AeTestNode.PASSED));

        assertEquals(Result.FAIL, node.evaluate(null));
    }

    @Test
    public void testChildFail() {
        AeNotNode node = new AeNotNode(List.of(AeTestNode.FAILED));

        assertEquals(Result.PASS, node.evaluate(null));

        assertEquals(Block.ANDESITE, Block.ANDESITE);
    }
}
