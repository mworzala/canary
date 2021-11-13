package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.mattworzala.canary.internal.assertion.Helper.assertFail;
import static com.mattworzala.canary.internal.assertion.Helper.assertSameResult;
import static com.mattworzala.canary.internal.assertion.node.AeTestNode.*;
import static com.mattworzala.canary.internal.assertion.node.AeTestNode.NODE_FAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AeNotNodeTest {
    private static Stream<Arguments> providePossibleResults() {
        return Stream.of(
                Arguments.of(NODE_PASS, RES_FAIL),
                Arguments.of(NODE_SOFT_PASS, RES_FAIL), //todo is this correct?
                Arguments.of(NODE_FAIL, RES_PASS)
        );
    }

    @ParameterizedTest
    @MethodSource("providePossibleResults")
    public void testResultCombinations(AeNode item, Result expected) {
        AeNotNode node = new AeNotNode(List.of(item));

        assertSameResult(expected, node.evaluate(null));
    }

    // Non-standard child count

    @Test
    public void testMissingChildShouldFailToEvaluate() {
        AeNotNode node = new AeNotNode(List.of());

        assertThrows(IllegalStateException.class, () -> node.evaluate(null));
    }

    @Test
    public void testExtraChildrenShouldBeIgnored() {
        AeNotNode node = new AeNotNode(List.of(NODE_PASS, NODE_PASS));

        assertFail(node.evaluate(null));
    }

    // toString

    @Test
    public void testToString() {
        AeNotNode node = new AeNotNode(List.of(NODE_FAIL));

        assertEquals("(NOT <test>)", node.toString());
    }

    @Test
    public void testMissingChildrenShouldToString() {
        AeNotNode node = new AeNotNode(List.of());

        assertEquals("(NOT <!>)", node.toString());
    }
}
