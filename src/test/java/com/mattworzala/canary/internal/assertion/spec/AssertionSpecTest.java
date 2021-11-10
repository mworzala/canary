package com.mattworzala.canary.internal.assertion.spec;

import org.junit.jupiter.api.Test;

import static com.mattworzala.canary.internal.assertion.Helper.*;
import static com.mattworzala.canary.internal.assertion.spec.AssertionSpec.*;

public class AssertionSpecTest {

    @Test
    public void testToEqualShouldUseEqualsFunction() {
        assertPass(toEqual(1, 1));
        assertPass(toEqual("123", "123"));

        assertFail(toEqual("123", "456"));
    }

    @Test
    public void testToEqualStrictShouldUseReferentialEquality() {
        assertPass(toEqualStrict(1, 1));
        String myString = "123";
        assertPass(toEqualStrict(myString, myString));

        assertFail(toEqualStrict("123", "456"));
        assertFail(toEqualStrict(new String("123"), new String("123")));
    }
}
