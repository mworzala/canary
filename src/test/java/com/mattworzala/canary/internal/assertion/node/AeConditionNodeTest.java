package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.AssertionCondition;
import com.mattworzala.canary.internal.assertion.Result;
import com.mattworzala.canary.internal.assertion.node.AeConditionNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AeConditionNodeTest {

    @Test
    public void testPassingConditionShouldPass() {
        AeConditionNode node = new AeConditionNode("test", () -> null, o -> true);

        Assertions.assertEquals(Result.PASS, node.evaluate(null));
    }

    @Test
    public void testFailingConditionShouldFail() {
        AssertionCondition assertionCondition = new AssertionCondition("test", o -> false);
        AeConditionNode node = new AeConditionNode("test", () -> null, o -> false);

        assertEquals(Result.FAIL, node.evaluate(null));
    }

    @Test
    public void testTargetShouldBeForwardedToPredicate() {
        AeConditionNode node = new AeConditionNode("test", () -> true, o -> (boolean) o);

        assertEquals(Result.PASS, node.evaluate(true));
    }

    // Condition problems

    @Test
    public void testExceptionInConditionShouldFail() {
        AeConditionNode node = new AeConditionNode("test", () -> null, o -> {
            throw new RuntimeException("Failure expected");
        });

        assertEquals(Result.FAIL, node.evaluate(null));
    }

    // toString

    @Test
    public void testToString() {
        AeConditionNode node = new AeConditionNode("test", () -> null, o -> {
            throw new IllegalStateException("Should not evaluate assertion for toString");
        });

        assertEquals("test", node.toString());
    }
}
