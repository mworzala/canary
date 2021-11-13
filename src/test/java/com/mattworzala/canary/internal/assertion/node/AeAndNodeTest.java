package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.mattworzala.canary.internal.assertion.Helper.assertPass;
import static com.mattworzala.canary.internal.assertion.Helper.assertSameResult;
import static com.mattworzala.canary.internal.assertion.node.AeTestNode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AeAndNodeTest {
    private static Stream<Arguments> providePossibleResults() {
        return Stream.of(
                Arguments.of(NODE_PASS, NODE_PASS, RES_PASS),
                Arguments.of(NODE_PASS, NODE_SOFT_PASS, RES_SOFT_PASS),
                Arguments.of(NODE_PASS, NODE_FAIL, RES_FAIL),

                Arguments.of(NODE_SOFT_PASS, NODE_PASS, RES_SOFT_PASS),
                Arguments.of(NODE_SOFT_PASS, NODE_SOFT_PASS, RES_SOFT_PASS),
                Arguments.of(NODE_SOFT_PASS, NODE_FAIL, RES_FAIL),

                Arguments.of(NODE_FAIL, NODE_PASS, RES_FAIL),
                Arguments.of(NODE_FAIL, NODE_SOFT_PASS, RES_FAIL),
                Arguments.of(NODE_FAIL, NODE_FAIL, RES_FAIL)
        );
    }

    @ParameterizedTest
    @MethodSource("providePossibleResults")
    public void testResultCombinations(AeNode lhs, AeNode rhs, Result expected) {
        AeAndNode node = new AeAndNode(List.of(lhs, rhs));

        assertSameResult(expected, node.evaluate(null));
    }

    // Non-standard child count

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void testMissingChildShouldFailToEvaluate(int count) {
        AeAndNode node = new AeAndNode(Collections.nCopies(count, NODE_PASS));

        assertThrows(IllegalStateException.class, () -> node.evaluate(null));
    }

    @Test
    public void testExtraChildrenShouldBeIgnored() {
        //TODO : AND should handle extra children successfully
        AeAndNode node = new AeAndNode(List.of(NODE_PASS, NODE_PASS, NODE_FAIL));

        assertPass(node.evaluate(null));
    }

    // toString

    @Test
    public void testToString() {
        AeAndNode node = new AeAndNode(List.of(NODE_PASS, NODE_PASS));

        assertEquals("(<test> AND <test>)", node.toString());
    }

    @Test
    public void testMissingChildrenShouldToString() {
        AeAndNode node = new AeAndNode(List.of());

        assertEquals("(<!> AND <!>)", node.toString());
    }
}
