package com.mattworzala.canary.internal.assertion.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.mattworzala.canary.internal.assertion.spec.AssertionSpec.*;

public class AssertionSpecTest {

    @Test
    public void testToEqualShouldUseEqualsFunction() {
        assertTrue(toEqual(1, 1));
        assertTrue(toEqual("123", "123"));

        assertFalse(toEqual("123", "456"));
    }

    @Test
    public void testToEqualStrictShouldUseReferentialEquality() {
        assertTrue(toEqualStrict(1, 1));
        String myString = "123";
        assertTrue(toEqualStrict(myString, myString));

        assertFalse(toEqualStrict("123", "456"));
        assertFalse(toEqualStrict(new String("123"), new String("123")));
    }
}
