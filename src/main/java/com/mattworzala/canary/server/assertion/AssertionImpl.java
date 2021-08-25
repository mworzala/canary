package com.mattworzala.canary.server.assertion;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class AssertionImpl<T, This extends AssertionImpl<T, This>> implements Supplier<AssertionResult> {


    protected Predicate<T> assertionTest = (T) -> true;
    protected boolean negate = false;
    protected final int DEFAULT_LIFESPAN = 100;
    protected int lifespan = DEFAULT_LIFESPAN;
    protected boolean reachedDefinitiveResult = false;

    private final T input;

    public AssertionImpl(T input) {
        this.input = input;
    }

    @Override
    public AssertionResult get() {
        lifespan--;

        boolean assertion = assertionTest.test(this.input);

        if (!negate) {
            if (assertion) {
                // if we aren't negating, and the assertion is true
                // then the test passes
                reachedDefinitiveResult = true;
                return AssertionResult.PASS;
            }
        } else {
            if (assertion) {
                // if we are negating, and the test passes, we fail
                reachedDefinitiveResult = true;
                return AssertionResult.FAIL;
            }
        }
        // otherwise
        if (lifespan > 0) {
            return AssertionResult.NO_RESULT;
        }
        reachedDefinitiveResult = true;
        return getEndResult();
    }

    public AssertionResult getEndResult() {
        if (!negate) {
            // without negate, the test can only fail at the end
            return AssertionResult.FAIL;
        }
        // with negate the test can only pass at the end
        return AssertionResult.PASS;
    }

    public boolean hasDefinitiveResult() {
        return this.reachedDefinitiveResult;
    }

    public This not() {
        this.negate = !this.negate;
        return (This) this;
    }

    public This isEqualToo(T v) {
        assertionTest = (T input) -> input == v;
        return (This) this;
    }


    public This and() {
        return (This) this;
    }
}
