package com.mattworzala.canary.internal.assertion;

import com.mattworzala.canary.api.supplier.ObjectSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record AssertionStep(
        @NotNull Type type,
        @Nullable String debugName,
        @Nullable ObjectSupplier supplier,
        @Nullable Result.Predicate<Object> condition) {

    // Control flow words
    public static final AssertionStep AND = new AssertionStep(Type.AND, null, null, null);
    public static final AssertionStep NOT = new AssertionStep(Type.NOT, null, null, null);
    public static final AssertionStep ALWAYS = new AssertionStep(Type.ALWAYS, null, null, null);
    public static final AssertionStep THEN = new AssertionStep(Type.THEN, null, null, null);

    public enum Type {
        CONDITION,
        AND,
        NOT,
        ALWAYS,
        THEN,
    }
}
