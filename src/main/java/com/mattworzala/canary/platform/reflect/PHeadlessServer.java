package com.mattworzala.canary.platform.reflect;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static com.mattworzala.canary.platform.util.ClassLoaders.MINESTOM;
import static com.mattworzala.canary.platform.util.ClassLoaders.loadClassRequired;
import static com.mattworzala.canary.platform.util.ReflectionUtils.invokeConstructor;
import static org.junit.platform.commons.util.ReflectionUtils.*;

@Environment(EnvType.PLATFORM)
public class PHeadlessServer {
    private static final Class<?> headlessServerClass = loadClassRequired(MINESTOM, "com.mattworzala.canary.server.HeadlessServer");

    // @formatter:off
    private static final Constructor<?> constructor = getDeclaredConstructor(headlessServerClass);
    public static @NotNull PHeadlessServer create() {
        return new PHeadlessServer(invokeConstructor(constructor));
    }
    // @formatter:on

    private final Object headlessServer;

    protected PHeadlessServer(Object headlessServer) {
        this.headlessServer = headlessServer;
    }

    // @formatter:off
    private static final Method start = getRequiredMethod(headlessServerClass, "start");
    public void start() {
        invokeMethod(start, headlessServer);
    }
    // @formatter:on

    // @formatter:off
    private static final Method stop = getRequiredMethod(headlessServerClass, "stop");
    public void stop() {
        invokeMethod(stop, headlessServer);
    }
    // @formatter:on

    // @formatter:off
    private static final Method createEnvironment = getRequiredMethod(headlessServerClass, "createEnvironment");
    public PTestEnvironment createEnvironment() {
        Object testEnvironment = invokeMethod(createEnvironment, headlessServer);
        return new PTestEnvironment(testEnvironment);
    }
    // @formatter:on

}
