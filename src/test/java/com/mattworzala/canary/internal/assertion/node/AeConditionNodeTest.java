package com.mattworzala.canary.internal.assertion.node;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.mattworzala.canary.internal.assertion.Helper.assertFail;
import static com.mattworzala.canary.internal.assertion.Helper.assertPass;
import static com.mattworzala.canary.internal.assertion.node.AeTestNode.RES_FAIL;
import static com.mattworzala.canary.internal.assertion.node.AeTestNode.RES_PASS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AeConditionNodeTest {

    @Test
    public void testPassingConditionShouldPass() {
        AeConditionNode node = new AeConditionNode("test", () -> null, o -> RES_PASS);

        assertPass(node.evaluate(null));
    }

    @Test
    public void testFailingConditionShouldFail() {
        AeConditionNode node = new AeConditionNode("test", () -> null, o -> RES_FAIL);

        assertFail(node.evaluate(null));
    }

    @Test
    public void testTargetShouldBeForwardedToPredicate() {
        AeConditionNode node = new AeConditionNode("test", () -> true, o -> (boolean) o ? RES_PASS : RES_FAIL);

        assertPass(node.evaluate(true));
    }

    // Condition problems

    @Test
    public void testExceptionInConditionShouldFail() {
        AeConditionNode node = new AeConditionNode("test", () -> null, o -> {
            throw new RuntimeException("Failure expected");
        });

        assertFail(node.evaluate(null));
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
