package com.mattworzala.canary.server;

import com.mattworzala.canary.api.TestEnvironment;
import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.env.TestEnvironmentImpl;
import com.mattworzala.canary.server.instance.ViewerInstance;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;

@Environment(EnvType.MINESTOM)
public class HeadlessServer {
    protected Instance instance;

    public void start() {

//        MinecraftServer.getExtensionManager().loadDynamicExtension()
        //todo find a better way to do this (see issue #3)
//        System.setProperty("minestom.extension.indevfolder.classes", "classes/java/main/");
//        System.setProperty("minestom.extension.indevfolder.resources", "resources/main/");

        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();
        initServer();
        minecraftServer.start("0.0.0.0", 25565); //todo should set to known random port + localhost instead for headless
    }

    public void initServer() {
        // Create spawning instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
//        InstanceContainer instance = instanceManager.createInstanceContainer();
//        instance.setChunkGenerator(new BasicGenerator());

        instance = new ViewerInstance();
        instanceManager.registerInstance(instance);

        //todo this isnt great, TestInstance should handle this
//        System.out.println("Force loading spawn chunks");
//        for (int x = -10; x <= 10; x++) {
//            for (int z = -10; z <= 10; z++) {
//                instance.loadChunk(x, z);
//            }
//        }
    }

    public void stop() {
        MinecraftServer.stopCleanly();
    }

    public TestEnvironment createEnvironment() {
        //todo eventually this will need to find an appropriate instance (or create), add the test structure, and then give an accurate environment.
        //todo we probably want to use a mechanism like `SharedInstance` where entities will be instance-localized and
        var instance = MinecraftServer.getInstanceManager().getInstances().stream().findFirst().get();
        return new TestEnvironmentImpl(instance);
    }
}
