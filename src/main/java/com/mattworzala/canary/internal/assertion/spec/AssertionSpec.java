package com.mattworzala.canary.internal.assertion.spec;

import static com.mattworzala.canary.internal.assertion.spec.GenSpec.*;

@GenSpec(operator = Object.class, supertype = "")
public class AssertionSpec {

    /*
            NOT value=1
            ^^^

            NOT: Expected value=1 to be false, value=1 was true

            `value=1` Expected value of 1, got 67



            value=1 AND value=2
                    ^^^

            AND: Expected LHS to pass, but it failed

            `value=1` Expected value of 1, got 67

         */

    @Condition("value=\"{0}\"")
    public static boolean toEqual(Object actual, Object expected) {
        return expected.equals(actual);
    }

    @Doc("""
            Expects the provided value to be referentially equal to the actual value. Tested using `==`.
            Note: {@link #toEqual} may be used to {@link Object#equals(Object)} equality.
            """)
    @Condition("value=\"{0}\"")
    public static boolean toEqualStrict(Object actual, @Doc("The expected value") Object expected) {
        return expected == actual;
    }

}
