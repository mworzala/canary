package com.mattworzala.canary.internal.util.reflect;

import com.mattworzala.canary.internal.util.safety.EnvType;
import com.mattworzala.canary.internal.util.safety.Env;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

import static com.mattworzala.canary.internal.util.ClassLoaders.MINESTOM;
import static com.mattworzala.canary.internal.util.ClassLoaders.loadClassRequired;
import static com.mattworzala.canary.internal.util.ReflectionUtils.invokeConstructor;
import static org.junit.platform.commons.util.ReflectionUtils.getDeclaredConstructor;

@Env(EnvType.PLATFORM)
public class ProxySandboxServer extends ProxyHeadlessServer {
    private static final Class<?> sandboxServerClass = loadClassRequired(MINESTOM, "com.mattworzala.canary.internal.server.sandbox.SandboxServer");

    // @formatter:off
    private static final Constructor<?> constructor = getDeclaredConstructor(sandboxServerClass);
    public static @NotNull ProxySandboxServer create() {return new ProxySandboxServer(invokeConstructor(constructor));}
    // @formatter:on

    public ProxySandboxServer(Object headlessServer) {
        super(headlessServer);
    }

    //todo other methods here
}
