package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static com.mattworzala.canary.internal.assertion.Helper.assertPass;
import static com.mattworzala.canary.internal.assertion.node.AeTestNode.NODE_PASS;
import static com.mattworzala.canary.internal.assertion.node.AeTestNode.RES_PASS;
import static org.junit.jupiter.api.Assertions.*;

public class AeNodeTest {

    // evaluate

    @Test
    public void testEvaluateShouldReturnSampleResult() {
        AeNode node = new AeTestNode(RES_PASS);

        assertPass(node.evaluate(null));
    }

    @Test
    public void testEvaluateShouldAddToHistory() {
        AeNode node = new AeTestNode(RES_PASS);

        // Start at 0
        assertEquals(0, node.history().count());

        // Increment by 1 per eval
        node.evaluate(null);
        assertEquals(1, node.history().count());
        node.evaluate(null);
        assertEquals(2, node.history().count());

        // Should now contain only PASS values
        assertTrue(node.history().allMatch(result -> result == RES_PASS));
    }

    // sample

    @Test
    public void testSampleShouldNotModifyHistory() {
        AeNode node = new AeTestNode(RES_PASS);

        // Start at 0
        assertEquals(0, node.history().count());

        // Remain at 0 after sample
        node.sample(null);
        assertEquals(0, node.history().count());
        node.sample(null);
        assertEquals(0, node.history().count());
    }

    // getChild

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void testGetChildShouldWorkWithOneOrMoreChildren(int count) {
        AeNode node = new AeTestNode(Collections.nCopies(count, NODE_PASS));

        for (int i = 0; i < count; i++) {
            assertEquals(NODE_PASS, node.getChild(i));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void testGetChildShouldDefaultToMissing(int count) {
        AeNode node = new AeTestNode(Collections.nCopies(count, NODE_PASS));

        assertEquals(AeNode.MISSING, node.getChild(-1));
        assertEquals(AeNode.MISSING, node.getChild(count));
    }

    // MISSING

    @Test
    public void testMissingToString() {
        assertEquals("<!>", AeNode.MISSING.toString());
    }

    @Test
    public void testMissingEvaluateShouldThrow() {
        assertThrows(IllegalStateException.class, () -> AeNode.MISSING.evaluate(null));
    }
}
