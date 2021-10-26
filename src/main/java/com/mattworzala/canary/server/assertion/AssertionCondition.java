package com.mattworzala.canary.server.assertion;

import java.util.function.Predicate;

public record AssertionCondition(String debugName, Predicate<Object> test) {
}
