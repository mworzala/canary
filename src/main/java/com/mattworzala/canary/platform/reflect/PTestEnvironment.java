package com.mattworzala.canary.platform.reflect;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.assertion.AssertionResult;

import java.lang.reflect.Method;

import static com.mattworzala.canary.platform.util.ClassLoaders.MINESTOM;
import static com.mattworzala.canary.platform.util.ClassLoaders.loadClassRequired;
import static org.junit.platform.commons.util.ReflectionUtils.getRequiredMethod;
import static org.junit.platform.commons.util.ReflectionUtils.invokeMethod;

@Environment(EnvType.PLATFORM)
public record PTestEnvironment(Object instance) {
    private static final Class<?> testEnvironmentClass = loadClassRequired(MINESTOM, "com.mattworzala.canary.api.TestEnvironment");

    // @formatter:off
    private static final Method startTesting = getRequiredMethod(testEnvironmentClass, "startTesting");
    public Enum<?> startTesting() {
        return (Enum<?>) invokeMethod(startTesting, instance);
    }
    // @formatter:on

    // @formatter:off
    private static final Method tick = getRequiredMethod(testEnvironmentClass, "tick");
    public AssertionResult tick() {
        return (AssertionResult) invokeMethod(tick, instance);
    }
    // @formatter:on

}
