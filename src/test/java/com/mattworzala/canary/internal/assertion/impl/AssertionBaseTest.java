package com.mattworzala.canary.internal.assertion.impl;

import com.mattworzala.canary.internal.assertion.AssertionStep;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mattworzala.canary.internal.assertion.node.AeTestNode.RES_PASS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertionBaseTest {

    @Test
    public void testAppendCondition() {
        List<AssertionStep> steps = new ArrayList<>();
        AssertionBase impl = new AssertionBase(null, steps);

        impl.appendCondition("test", o -> RES_PASS);

        assertEquals(1, steps.size());

        AssertionStep created = steps.get(0);
        assertEquals(AssertionStep.Type.CONDITION, created.type());
        assertEquals("test", created.debugName());
    }
}
