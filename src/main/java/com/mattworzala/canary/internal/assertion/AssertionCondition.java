package com.mattworzala.canary.internal.assertion;

import java.util.function.Predicate;

public record AssertionCondition(String debugName, Predicate<Object> test) {
}
