package com.mattworzala.canary.junit.discovery;

import java.util.function.Predicate;

public class IsPotentialCanaryTestClass implements Predicate<Class<?>> {
    @Override
    public boolean test(Class<?> aClass) {
        return true;
    }
}
