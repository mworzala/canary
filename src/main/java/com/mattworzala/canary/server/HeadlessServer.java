package com.mattworzala.canary.server;

import com.mattworzala.canary.api.TestEnvironment;
import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.env.TestEnvironmentImpl;
import com.mattworzala.canary.server.execution.TestCoordinator;
import net.minestom.server.MinecraftServer;

@Environment(EnvType.MINESTOM)
public class HeadlessServer {
    protected static boolean headless = true;

    public static boolean isHeadless() {
        return headless;
    }

    private final TestCoordinator coordinator = new TestCoordinator();

    public final void start(int port) {

//        MinecraftServer.getExtensionManager().loadDynamicExtension()
        //todo find a better way to do this (see issue #3)
//        System.setProperty("minestom.extension.indevfolder.classes", "classes/java/main/");
//        System.setProperty("minestom.extension.indevfolder.resources", "resources/main/");

        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();
        initServer();
        System.out.println("Starting on port: " + port);
        minecraftServer.start("0.0.0.0", port); //todo set localhost in headless environment?
    }

    public TestCoordinator getTestCoordinator() {
        return coordinator;
    }

    protected void initServer() { }

    public void stop() {
        MinecraftServer.stopCleanly();
    }

    public TestCoordinator getCoordinator() {
        return coordinator;
    }

    public TestEnvironment createEnvironment() {
        //todo eventually this will need to find an appropriate instance (or create), add the test structure, and then give an accurate environment.
        //todo we probably want to use a mechanism like `SharedInstance` where entities will be instance-localized and
        var instance = MinecraftServer.getInstanceManager().getInstances().stream().findFirst().get();
        return new TestEnvironmentImpl(null);
    }
}
