package com.mattworzala.canary.platform.reflect;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;

import java.lang.reflect.Method;

import static com.mattworzala.canary.platform.util.ClassLoaders.MINESTOM;
import static com.mattworzala.canary.platform.util.ClassLoaders.loadClassRequired;
import static org.junit.platform.commons.util.ReflectionUtils.*;

@Environment(EnvType.PLATFORM)
public record PTestEnvironment(Object instance) {
    private static final Class<?> testEnvironmentClass = loadClassRequired(MINESTOM, "com.mattworzala.canary.api.TestEnvironment");

    // @formatter:off
    private static final Method startTesting = getRequiredMethod(testEnvironmentClass, "startTesting");
    public void startTesting() {
        invokeMethod(startTesting, instance);
    }
    // @formatter:on

}
