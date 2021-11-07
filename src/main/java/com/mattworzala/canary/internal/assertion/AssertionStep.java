package com.mattworzala.canary.internal.assertion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AssertionStep(@NotNull Type type, @Nullable AssertionCondition condition) {
    // Control flow words
    public static final AssertionStep AND = new AssertionStep(Type.AND, null);

    public static final AssertionStep NOT = new AssertionStep(Type.NOT, null);

    public static final AssertionStep ALWAYS = new AssertionStep(Type.ALWAYS, null);

    public static final AssertionStep THEN = new AssertionStep(Type.THEN, null);

    public enum Type {
        CONDITION,
        AND,
        NOT,
        ALWAYS,
        THEN,
    }
}
