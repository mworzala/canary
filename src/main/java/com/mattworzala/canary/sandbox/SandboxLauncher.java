package com.mattworzala.canary.sandbox;

import com.mattworzala.canary.test.junit.CanaryTestEngine;
import com.mattworzala.canary.test.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.test.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.test.sandbox.SandboxTestEnvironment;
import com.mattworzala.canary.test.sandbox.SandboxTestExecutor;
import net.minestom.server.Bootstrap;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

public class SandboxLauncher {
    public static void main(String[] args) throws Exception {


        //todo there is a big issue here. Test discovery will load the test classes before this point and into the main class loader
        //     In this case all of it's Minestom classes will be loaded into the main class loader and then re loaded by Minestom
        //      into the Minestom classloader. That will not be valid.
        //     In an ideal world, I believe we need to load the test classes into the Minestom classloader and allow all the
        //      test utils such as assertions, test manipulation, etc to be loaded in the Minestom classloader. That being said,
        //      I am not sure if thats possible, since the non-Minestom code probably needs to use it, which would mean it will
        //      be loaded into both unless all code is loaded into the Minestom classloader.
        //     Seems like the solution here is probably to load everything besides Mixins into the Minestom classloader, but this means
        //      junit needs to be convinced to load into the Minestom classloader.

        MinestomRootClassLoader classLoader = MinestomRootClassLoader.getInstance();
        // Protect junit
        classLoader.protectedPackages.add("org.junit");
        // Protect all packages besides `server`.
        classLoader.protectedPackages.add("com.mattworzala.canary.test");
        classLoader.protectedPackages.add("com.mattworzala.canary.sandbox");

        SandboxTestEnvironment.getInstance().discover();

        //todo find a better way to do this
        System.setProperty("minestom.extension.indevfolder.classes", "classes/java/main/");
        System.setProperty("minestom.extension.indevfolder.resources", "resources/main/");
        Bootstrap.bootstrap("com.mattworzala.canary.server.SandboxServer", args);

        // Can do stuff here
    }
}
