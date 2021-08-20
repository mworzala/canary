package com.mattworzala.canary.platform.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;

@Environment(EnvType.GLOBAL)
@ApiStatus.Internal
public class ReflectionUtils {
    private ReflectionUtils() {}

    public static boolean hasNoParameters(@NotNull Method method) {
        return method.getParameterCount() == 0;
    }

    public static boolean hasParameterTypes(@NotNull Method method, @NotNull Class<?>... types) {
        return Arrays.equals(method.getParameterTypes(), types);
    }

}
