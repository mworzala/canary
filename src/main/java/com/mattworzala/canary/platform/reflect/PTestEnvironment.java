package com.mattworzala.canary.platform.reflect;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.assertion.AssertionResult;
import com.mattworzala.canary.server.givemeahome.Structure;

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
    private static final Method loadWorldData = getRequiredMethod(testEnvironmentClass, "loadWorldData", String.class, int.class, int.class, int.class);
    public Object loadWorldData(String fileName, int originX, int originY, int originZ) {
        return invokeMethod(loadWorldData, instance, fileName, originX, originY, originZ);
    }
    // @formatter:on

    // @formatter:off
    private static final Method tick = getRequiredMethod(testEnvironmentClass, "tick");
    public AssertionResult tick() {
        return (AssertionResult) invokeMethod(tick, instance);
    }
    // @formatter:on

}
