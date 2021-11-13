package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.mattworzala.canary.internal.assertion.Helper.*;
import static com.mattworzala.canary.internal.assertion.node.AeTestNode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AeAlwaysNodeTest {
    private static Stream<Arguments> providePossibleResults() {
        return Stream.of(
                Arguments.of(NODE_PASS, RES_SOFT_PASS),
                Arguments.of(NODE_SOFT_PASS, RES_SOFT_PASS),
                Arguments.of(NODE_FAIL, RES_FAIL)
        );
    }

    @ParameterizedTest
    @MethodSource("providePossibleResults")
    public void testResultCombinations(AeNode item, Result expected) {
        AeAlwaysNode node = new AeAlwaysNode(List.of(item));

        assertSameResult(expected, node.evaluate(null));
    }

    // Non-standard child count

    @Test
    public void testMissingChildShouldFailToEvaluate() {
        AeAlwaysNode node = new AeAlwaysNode(List.of());

        assertThrows(IllegalStateException.class, () -> node.evaluate(null));
    }

    @Test
    public void testExtraChildrenShouldBeIgnored() {
        AeAlwaysNode node = new AeAlwaysNode(List.of(NODE_PASS, NODE_FAIL));

        assertSoftPass(node.evaluate(null));
    }

    // toString

    @Test
    public void testToString() {
        AeAlwaysNode node = new AeAlwaysNode(List.of(NODE_FAIL));

        assertEquals("(ALWAYS <test>)", node.toString());
    }

    @Test
    public void testMissingChildrenShouldToString() {
        AeAlwaysNode node = new AeAlwaysNode(List.of());

        assertEquals("(ALWAYS <!>)", node.toString());
    }

    // Result caching behavior

    @Test
    public void testCacheFailure() {
        AeTestNode testNode = new AeTestNode(RES_PASS);

        AeAlwaysNode node = new AeAlwaysNode(List.of(testNode));

        // Child passing, should check again
        assertSoftPass(node.evaluate(null));

        // Child soft passing, should check again
        testNode.result = RES_SOFT_PASS;
        assertSoftPass(node.evaluate(null));

        // Child failing
        testNode.result = RES_FAIL;
        assertFail(node.evaluate(null));

        // Child now passing, should remain fail
        testNode.result = RES_PASS;
        assertFail(node.evaluate(null));
    }

    @Test
    public void testChildShouldStopBeingTestedAfterFail() {
        AeTestNode testNode = new AeTestNode(RES_FAIL);

        AeAlwaysNode node = new AeAlwaysNode(List.of(testNode));

        // Child fail, should have been tested once
        node.evaluate(null);
        assertEquals(1, testNode.history().count());

        // Child should never be tested again (history should remain 1)
        testNode.result = RES_PASS;
        node.evaluate(null);
        assertEquals(1, testNode.history().count());

        // Child failing
        testNode.result = RES_SOFT_PASS;
        node.evaluate(null);
        assertEquals(1, testNode.history().count());
    }
}





























