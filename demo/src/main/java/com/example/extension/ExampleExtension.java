package com.example.extension;

import com.example.extension.command.EntityCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;

public class ExampleExtension extends Extension {
    @Override
    public void initialize() {
        System.out.println("Hello from example!");

        registerCommands();
    }

    @Override
    public void terminate() {

    }

    private void registerCommands() {
        var commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new EntityCommand());
    }
}
