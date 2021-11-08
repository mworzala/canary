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

import static com.mattworzala.canary.internal.assertion.node.AeTestNode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AeAndNodeTest {
    private static Stream<Arguments> providePossibleResults() {
        return Stream.of(
                Arguments.of(PASS, PASS, Result.PASS),
                Arguments.of(PASS, SOFT_PASS, Result.SOFT_PASS),
                Arguments.of(PASS, FAIL, Result.FAIL),

                Arguments.of(SOFT_PASS, PASS, Result.SOFT_PASS),
                Arguments.of(SOFT_PASS, SOFT_PASS, Result.SOFT_PASS),
                Arguments.of(SOFT_PASS, FAIL, Result.FAIL),

                Arguments.of(FAIL, PASS, Result.FAIL),
                Arguments.of(FAIL, SOFT_PASS, Result.FAIL),
                Arguments.of(FAIL, FAIL, Result.FAIL)
        );
    }

    @ParameterizedTest
    @MethodSource("providePossibleResults")
    public void testResultCombinations(AeNode lhs, AeNode rhs, Result expected) {
        AeAndNode node = new AeAndNode(List.of(lhs, rhs));

        assertEquals(expected, node.evaluate(null));
    }

    // Non-standard child count

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void testMissingChildShouldFailToEvaluate(int count) {
        AeAndNode node = new AeAndNode(Collections.nCopies(count, PASS));

        assertThrows(IllegalStateException.class, () -> node.evaluate(null));
    }

    @Test
    public void testExtraChildrenShouldBeIgnored() {
        AeAndNode node = new AeAndNode(List.of(PASS, PASS, PASS));

        assertEquals(Result.PASS, node.evaluate(null));
    }

    // toString

    @Test
    public void testToString() {
        AeAndNode node = new AeAndNode(List.of(PASS, PASS));

        assertEquals("(<test> AND <test>)", node.toString());
    }

    @Test
    public void testMissingChildrenShouldToString() {
        AeAndNode node = new AeAndNode(List.of());

        assertEquals("(<!> AND <!>)", node.toString());
    }
}
