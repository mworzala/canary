package com.mattworzala.canary.test.junit.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtils {
    private ReflectionUtils() {}

    public static boolean hasNoParameters(@NotNull Method method) {
        return method.getParameterCount() == 0;
    }

    public static boolean hasParameterTypes(@NotNull Method method, @NotNull Class<?>... types) {
        return Arrays.equals(method.getParameterTypes(), types);
    }

}
