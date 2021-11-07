package com.mattworzala.canary.internal.assertion;

public class Context<T> {
    public final T actual;

    public Context(T actual) {
        this.actual = actual;
    }

}
