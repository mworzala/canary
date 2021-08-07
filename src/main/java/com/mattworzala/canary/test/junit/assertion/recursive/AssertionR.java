package com.mattworzala.canary.test.junit.assertion.recursive;

public class AssertionR<T, A extends AssertionR<T, A>> {

    public A not() {
        return (A) this;
    }

    public A and() {
        return (A) this;
    }
}
