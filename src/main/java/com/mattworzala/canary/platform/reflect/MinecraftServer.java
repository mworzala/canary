package com.mattworzala.canary.platform.reflect;

import com.mattworzala.canary.platform.util.ClassLoaders;

import java.lang.reflect.Method;

import static org.junit.platform.commons.util.ReflectionUtils.*;

public record MinecraftServer(Object minecraftServer) {
    private static final Class<?> minecraftServerClass = ClassLoaders.loadClassRequired(ClassLoaders.MINESTOM, "net.minestom.server.MinecraftServer");

    private static final Method init = getRequiredMethod(minecraftServerClass, "init");
    public static MinecraftServer init() {
        Object minecraftServer = invokeMethod(init, null);
        return new MinecraftServer(minecraftServer);
    }

    private static final Method stopCleanly = getRequiredMethod(minecraftServerClass, "stopCleanly");
    public static void stopCleanly() {
        invokeMethod(stopCleanly, null);
    }

    private static final Method start = getRequiredMethod(minecraftServerClass, "start", String.class, int.class);
    public void start() {
        invokeMethod(start, minecraftServer, "0.0.0.0", 25565);
    }
}
