package com.mattworzala.canary.server.assertion;

public class AssertionImpl<T, A extends AssertionImpl<T, A>> {

    public A not() {
        return (A) this;
    }

    public A and() {
        return (A) this;
    }
}
