package com.mattworzala.canary.server.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.instance.Instance;

import java.util.UUID;

public class InstanceCommand extends Command {
    public InstanceCommand() {
        super("instance");

        var instances = MinecraftServer.getInstanceManager().getInstances().stream().map(Instance::getUniqueId).map(UUID::toString).toArray(String[]::new);
        var instanceId = ArgumentType.Word("instance").from(instances);

        addSyntax((sender, context) -> {
            if (!sender.isPlayer()) {
                System.out.println("Cannot teleport console");
                return;
            }

            var instance = MinecraftServer.getInstanceManager().getInstance(UUID.fromString(context.get(instanceId)));
            sender.asPlayer().setInstance(instance, sender.asPlayer().getPosition());
        }, instanceId);
    }
}
