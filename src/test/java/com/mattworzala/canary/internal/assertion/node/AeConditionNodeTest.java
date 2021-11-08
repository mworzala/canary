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
        AssertionCondition assertionCondition = new AssertionCondition("test", o -> true);
        AeConditionNode node = new AeConditionNode(assertionCondition);

        Assertions.assertEquals(Result.PASS, node.evaluate(null));
    }

    @Test
    public void testFailingConditionShouldFail() {
        AssertionCondition assertionCondition = new AssertionCondition("test", o -> false);
        AeConditionNode node = new AeConditionNode(assertionCondition);

        assertEquals(Result.FAIL, node.evaluate(null));
    }

    @Test
    public void testTargetShouldBeForwardedToPredicate() {
        AssertionCondition assertionCondition = new AssertionCondition("test", o -> (boolean) o);
        AeConditionNode node = new AeConditionNode(assertionCondition);

        assertEquals(Result.PASS, node.evaluate(true));
    }

    // Condition problems

    @Test
    public void testExceptionInConditionShouldFail() {
        AssertionCondition assertionCondition = new AssertionCondition("test", o -> {
            throw new RuntimeException("Failure expected");
        });

        AeConditionNode node = new AeConditionNode(assertionCondition);

        assertEquals(Result.FAIL, node.evaluate(null));
    }

    @Test
    public void testNullConditionShouldThrow() {
        AssertionCondition assertionCondition = new AssertionCondition("test", null);

        AeConditionNode node = new AeConditionNode(assertionCondition);
        //todo should this exception occur when constructing the node? probably.
        //     potentially AssertionCondition#condition could just be @NotNull

        assertThrows(NullPointerException.class, () -> node.evaluate(null));
    }

    // toString

    @Test
    public void testToString() {
        AeConditionNode node = new AeConditionNode(new AssertionCondition("test", o -> { throw new IllegalStateException("Should not evaluate assertion for toString"); }));

        assertEquals("test", node.toString());
    }
}
