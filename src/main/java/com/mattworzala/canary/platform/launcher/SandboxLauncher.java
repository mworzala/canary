package com.mattworzala.canary.platform.launcher;

import com.mattworzala.canary.platform.givemeahome.SandboxTestEnvironment;
import com.mattworzala.canary.platform.reflect.PSandboxServer;
import com.mattworzala.canary.platform.util.MinestomMixin;

public class SandboxLauncher {
    public static void main(String[] args) {
        MinestomMixin.inject(args); //"--mixin", "mixin.canary.base.json"

        var server = PSandboxServer.create();

        SandboxTestEnvironment.getInstance().discover();

        server.start();
        // Stopped by other means
    }
}
