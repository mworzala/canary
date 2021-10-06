package com.mattworzala.canary.server.assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AssertionImpl<T, This extends AssertionImpl<T, This>> {


    protected Predicate<T> assertionTest = (T) -> true;
    protected boolean negate = false;
    protected final int DEFAULT_LIFESPAN = 100;
    protected int lifespan = DEFAULT_LIFESPAN;
    protected boolean reachedDefinitiveResult = false;
    protected AssertionResult finalResult = null;

    protected Function<T, String> assertionFormatter = (T a) -> "";

    private final T input;


    private List<String> logs;

    public AssertionImpl(T input) {
        this.input = input;
        logs = new ArrayList<>();
    }

    public void tick() {
        if (reachedDefinitiveResult) {
            throw new RuntimeException("Should not tick an assertion which has reached a definitive result"); //todo this should be a silent return, but i want to see during development if I wrote good code :)
        }

        boolean assertion = assertionTest.test(this.input);
        String log = assertionFormatter.apply(this.input);
        logs.add(log);

        if (!negate) {
            if (assertion) {
                // if we aren't negating, and the assertion is true
                // then the test passes
                reachedDefinitiveResult = true;
                finalResult = AssertionResult.PASS;
                return;
            }
        } else {
            if (assertion) {
                // if we are negating, and the test passes, we fail
                reachedDefinitiveResult = true;
                finalResult = AssertionResult.FAIL;
                throw new AssertionError(log);
            }
        }

        // otherwise
        if (--lifespan > 0) {
            return;
        }

        reachedDefinitiveResult = true;
        finalResult = getEndResult();
        if (finalResult == AssertionResult.FAIL) {
            throw new AssertionError("Ran out of time!");
        }
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

    public This isEqual(T v) {
        assertionTest = (T input) -> input.equals(v);
        return (This) this;
    }

    public This isStrictEqual(T v) {
        assertionTest = (T input) -> input == v;
        return (This) this;
    }

    public List<String> getLogs() {
        return logs;
    }

    protected This self() {
        return (This) this;
    }
}
