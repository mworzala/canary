package com.mattworzala.canary.platform.reflect;

import com.mattworzala.canary.platform.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import static com.mattworzala.canary.platform.util.ClassLoaders.MINESTOM;
import static com.mattworzala.canary.platform.util.ClassLoaders.loadClassRequired;
import static com.mattworzala.canary.platform.util.ReflectionUtils.invokeConstructor;
import static org.junit.platform.commons.util.ReflectionUtils.*;

@Environment(EnvType.PLATFORM)
public class ProxyHeadlessServer {
    private static final Class<?> headlessServerClass = loadClassRequired(MINESTOM, "com.mattworzala.canary.server.HeadlessServer");

    // @formatter:off
    private static final Constructor<?> constructor = getDeclaredConstructor(headlessServerClass);
    public static @NotNull ProxyHeadlessServer create() {
        return new ProxyHeadlessServer(invokeConstructor(constructor));
    }
    // @formatter:on

    private final Object headlessServer;

    protected ProxyHeadlessServer(Object headlessServer) {
        this.headlessServer = headlessServer;
    }

    // @formatter:off
    private static final Method start = getRequiredMethod(headlessServerClass, "start", int.class);
    public void start(int port) {
        invokeMethod(start, headlessServer, port);
    }
    // @formatter:on

    // @formatter:off
    private static final Method stop = getRequiredMethod(headlessServerClass, "stop");
    public void stop() {
        invokeMethod(stop, headlessServer);
    }
    // @formatter:on

    // @formatter:off
    private static final Method getTestCoordinator = getRequiredMethod(headlessServerClass, "getTestCoordinator");
    public ProxyTestCoordinator getTestCoordinator() {
        Object testCoordinator = invokeMethod(getTestCoordinator, headlessServer);
        return new ProxyTestCoordinator(testCoordinator);
    }
    // @formatter:on


}
