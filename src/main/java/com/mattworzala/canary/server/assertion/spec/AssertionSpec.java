package com.mattworzala.canary.server.assertion.spec;

import static com.mattworzala.canary.server.assertion.spec.GenSpec.*;

@GenSpec(operator = Object.class, supertype = "")
/* Special case of no supplier here. */
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

    @Condition("value=\"{0}\"")
    public static boolean toEqualStrict(Object actual, Object expected) {
        return expected == actual;
    }

}
