package com.mattworzala.canary.internal.util;

import com.mattworzala.canary.internal.util.safety.EnvType;
import com.mattworzala.canary.internal.util.safety.Env;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@Env(EnvType.GLOBAL)
@ApiStatus.Internal
public class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static boolean hasNoParameters(@NotNull Method method) {
        return method.getParameterCount() == 0;
    }

    public static boolean hasParameterTypes(@NotNull Method method, @NotNull Class<?>... types) {
        return Arrays.equals(method.getParameterTypes(), types);
    }

    public static Object invokeConstructor(Constructor<?> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void invokeMethodOptionalParameter(Method method, Object instance, Object optionalParam) {
        if (method.getParameterCount() == 1) {
            org.junit.platform.commons.util.ReflectionUtils.invokeMethod(method, instance, optionalParam);
        } else {
            org.junit.platform.commons.util.ReflectionUtils.invokeMethod(method, instance);
        }
    }
}
