package com.mattworzala.canary.internal.assertion;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Helper {

    public static void assertPass(Result actual) {
        assertTrue(actual.isPass(), "expected PASS, was " + (actual.isFail() ? "FAIL" : "SOFT_PASS"));
    }

    public static void assertSoftPass(Result actual) {
        assertTrue(actual.isSoftPass(), "expected SOFT_PASS, was " + (actual.isFail() ? "FAIL" : "PASS"));
    }

    public static void assertFail(Result actual) {
        assertTrue(actual.isFail(), "expected FAIL, was " + (actual.isPass() ? "PASS" : "SOFT_PASS"));
    }

    /**
     * Ensures the two values have the same value, without comparing details such as reason or cause for failures.
     */
    public static void assertSameResult(Result expected, Result actual) {
        assertTrue((expected.isPass() && actual.isPass()) ||
                (expected.isSoftPass() && actual.isSoftPass()) ||
                (expected.isFail() && actual.isFail()));
    }
}
