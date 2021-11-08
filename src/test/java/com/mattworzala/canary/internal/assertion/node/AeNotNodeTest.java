package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.mattworzala.canary.internal.assertion.node.AeTestNode.*;
import static com.mattworzala.canary.internal.assertion.node.AeTestNode.FAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AeNotNodeTest {
    private static Stream<Arguments> providePossibleResults() {
        return Stream.of(
                Arguments.of(PASS, Result.FAIL),
                Arguments.of(SOFT_PASS, Result.FAIL), //todo is this correct?
                Arguments.of(FAIL, Result.PASS)
        );
    }

    @ParameterizedTest
    @MethodSource("providePossibleResults")
    public void testResultCombinations(AeNode item, Result expected) {
        AeNotNode node = new AeNotNode(List.of(item));

        assertEquals(expected, node.evaluate(null));
    }

    // Non-standard child count

    @Test
    public void testMissingChildShouldFailToEvaluate() {
        AeNotNode node = new AeNotNode(List.of());

        assertThrows(IllegalStateException.class, () -> node.evaluate(null));
    }

    @Test
    public void testExtraChildrenShouldBeIgnored() {
        AeNotNode node = new AeNotNode(List.of(PASS, PASS));

        assertEquals(Result.FAIL, node.evaluate(null));
    }

    // toString

    @Test
    public void testToString() {
        AeNotNode node = new AeNotNode(List.of(FAIL));

        assertEquals("(NOT <test>)", node.toString());
    }

    @Test
    public void testMissingChildrenShouldToString() {
        AeNotNode node = new AeNotNode(List.of());

        assertEquals("(NOT <!>)", node.toString());
    }
}
