package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.mattworzala.canary.internal.assertion.node.AeTestNode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AeAlwaysNodeTest {
    private static Stream<Arguments> providePossibleResults() {
        return Stream.of(
                Arguments.of(PASS, Result.SOFT_PASS),
                Arguments.of(SOFT_PASS, Result.SOFT_PASS),
                Arguments.of(FAIL, Result.FAIL)
        );
    }

    @ParameterizedTest
    @MethodSource("providePossibleResults")
    public void testResultCombinations(AeNode item, Result expected) {
        AeAlwaysNode node = new AeAlwaysNode(List.of(item));

        assertEquals(expected, node.evaluate(null));
    }

    // Non-standard child count

    @Test
    public void testMissingChildShouldFailToEvaluate() {
        AeAlwaysNode node = new AeAlwaysNode(List.of());

        assertThrows(IllegalStateException.class, () -> node.evaluate(null));
    }

    @Test
    public void testExtraChildrenShouldBeIgnored() {
        AeAlwaysNode node = new AeAlwaysNode(List.of(PASS, FAIL));

        assertEquals(Result.SOFT_PASS, node.evaluate(null));
    }

    // toString

    @Test
    public void testToString() {
        AeAlwaysNode node = new AeAlwaysNode(List.of(FAIL));

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
        AeTestNode testNode = new AeTestNode(Result.PASS);

        AeAlwaysNode node = new AeAlwaysNode(List.of(testNode));

        // Child passing, should check again
        assertEquals(Result.SOFT_PASS, node.evaluate(null));

        // Child soft passing, should check again
        testNode.result = Result.SOFT_PASS;
        assertEquals(Result.SOFT_PASS, node.evaluate(null));

        // Child failing
        testNode.result = Result.FAIL;
        assertEquals(Result.FAIL, node.evaluate(null));

        // Child now passing, should remain fail
        testNode.result = Result.PASS;
        assertEquals(Result.FAIL, node.evaluate(null));
    }

    @Test
    public void testChildShouldStopBeingTestedAfterFail() {
        AeTestNode testNode = new AeTestNode(Result.FAIL);

        AeAlwaysNode node = new AeAlwaysNode(List.of(testNode));

        // Child fail, should have been tested once
        node.evaluate(null);
        assertEquals(1, testNode.history().count());

        // Child should never be tested again (history should remain 1)
        testNode.result = Result.PASS;
        node.evaluate(null);
        assertEquals(1, testNode.history().count());

        // Child failing
        testNode.result = Result.SOFT_PASS;
        node.evaluate(null);
        assertEquals(1, testNode.history().count());
    }
}





























