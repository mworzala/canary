package com.mattworzala.canary.platform.reflect;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

import static com.mattworzala.canary.platform.util.ClassLoaders.MINESTOM;
import static com.mattworzala.canary.platform.util.ClassLoaders.loadClassRequired;
import static com.mattworzala.canary.platform.util.ReflectionUtils.invokeConstructor;
import static org.junit.platform.commons.util.ReflectionUtils.getDeclaredConstructor;

public class PSandboxServer extends PHeadlessServer {
    private static final Class<?> sandboxServerClass = loadClassRequired(MINESTOM, "com.mattworzala.canary.server.SandboxServer");

    // @formatter:off
    private static final Constructor<?> constructor = getDeclaredConstructor(sandboxServerClass);
    public static @NotNull PSandboxServer create() {return new PSandboxServer(invokeConstructor(constructor));}
    // @formatter:on

    public PSandboxServer(Object headlessServer) {
        super(headlessServer);
    }

    //todo other methods here
}
