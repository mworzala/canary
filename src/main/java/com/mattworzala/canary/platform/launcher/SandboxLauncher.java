package com.mattworzala.canary.platform.launcher;

import com.mattworzala.canary.platform.givemeahome.SandboxTestEnvironment;
import net.minestom.server.Bootstrap;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;

public class SandboxLauncher {
    public static void main(String[] args) throws Exception {
        MinestomRootClassLoader classLoader = MinestomRootClassLoader.getInstance();
        classLoader.protectedPackages.add("org.junit");
        classLoader.protectedPackages.add("com.mattworzala.canary.platform");

        SandboxTestEnvironment.getInstance().discover();

        //todo find a better way to do this
        System.setProperty("minestom.extension.indevfolder.classes", "classes/java/main/");
        System.setProperty("minestom.extension.indevfolder.resources", "resources/main/");
        Bootstrap.bootstrap("com.mattworzala.canary.server.SandboxServer", args);

        // Can do stuff here
    }
}
