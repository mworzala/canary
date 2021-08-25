package com.mattworzala.canary.server.assertion;

import java.util.function.Function;
import java.util.function.Predicate;

public class AssertionImpl<T, This extends AssertionImpl<T, This>> implements Function<T, AssertionResult>{

    protected Function<T, T> inputModifier = (T t) -> (T) t;
    protected Predicate<T> assertionTest = (T) -> true;
    protected Predicate<Boolean> outputModifier = (Boolean b) -> b;

    @Override
    public AssertionResult apply(T input) {
        var modifiedInput = inputModifier.apply(input);
        Boolean assertion = assertionTest.test(modifiedInput);
        var modifiedOutput = outputModifier.test(assertion);
        if (modifiedOutput) {
            return AssertionResult.PASS;
        }
        return AssertionResult.FAIL;

    }
    public This not() {
        this.outputModifier = outputModifier.negate();
        return (This) this;
    }

    public This isEqualToo(T v) {
        assertionTest = (T input) -> input == v;
        return (This) this;
    }

//    public A not() {
//        return (A) this;
//    }

    public This and() {
        return (This) this;
    }
}
