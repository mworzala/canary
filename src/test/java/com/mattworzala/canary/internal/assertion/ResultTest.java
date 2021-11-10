package com.mattworzala.canary.internal.assertion;

import org.junit.jupiter.api.Test;

import static com.mattworzala.canary.internal.assertion.node.AeTestNode.RES_FAIL;
import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {

    // This test is to highlight when this behavior is changed, not test any particular correctness.
    @Test
    public void testResultImplementationDetail() {
        assertSame(Result.Pass(), Result.Pass());
        assertSame(Result.SoftPass(), Result.SoftPass());

        assertNotSame(Result.Fail("reason"), Result.Fail("reason"));
    }

    @Test
    public void testFailEqualsAndHashCode() {
        // Same reason
        assertEquals(Result.Fail("reason"), Result.Fail("reason"));
        assertEquals(Result.Fail("reason").hashCode(), Result.Fail("reason").hashCode());
        assertNotSame(Result.Fail("reason"), Result.Fail("reason"));

        // Different reason
        assertNotEquals(Result.Fail("reason"), Result.Fail("other reason"));

        // Same reason & cause
        assertEquals(Result.Fail("reason", RES_FAIL), Result.Fail("reason", RES_FAIL));
        assertEquals(Result.Fail("reason", RES_FAIL).hashCode(), Result.Fail("reason", RES_FAIL).hashCode());
        assertNotSame(Result.Fail("reason", RES_FAIL), Result.Fail("reason", RES_FAIL));

        // Different cause
        assertNotEquals(Result.Fail("reason", RES_FAIL), Result.Fail("reason", Result.Fail("another reason")));
        assertNotEquals(Result.Fail("reason", RES_FAIL).hashCode(), Result.Fail("reason", Result.Fail("another reason")).hashCode());
    }
}
