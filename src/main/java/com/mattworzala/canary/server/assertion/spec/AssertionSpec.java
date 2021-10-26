package com.mattworzala.canary.server.assertion.spec;

import static com.mattworzala.canary.server.assertion.spec.GenSpec.*;

@GenSpec(supplierType = Object.class, supertype = "")
public class AssertionSpec {

    @Condition("value='{0}'")
    public static boolean toEqual(Object actual, Object expected) {
        return expected.equals(actual);
    }

    @Condition("value='{0}'")
    public static boolean toEqualStrict(Object actual, Object expected) {
        return expected == actual;
    }

}
