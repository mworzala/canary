package com.mattworzala.canary.internal.server.sandbox.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EntityCommand extends Command {


    public EntityCommand() {
        super("entity");

        var instances = MinecraftServer.getInstanceManager().getInstances().stream().map(Instance::getUniqueId).map(UUID::toString).toArray(String[]::new);
        var instanceId = ArgumentType.Word("instance").from(instances);

        addSyntax((sender, context) -> {
            var target = context.get(instanceId);
            var instance = MinecraftServer.getInstanceManager().getInstance(UUID.fromString(target));
//            var instance = sender.asPlayer().getInstance();

            EntityCreature entity = new EntityCreature(EntityType.ZOMBIE);
            entity.addAIGroup(new EntityAIGroupBuilder()
                    .addGoalSelector(new TestGoal(entity))
                    .build());
            entity.setInstance(instance, new Vec(12, 41, 22));
//            entity.getNavigator().setPathTo(new Vec(42, 41, 22), true);
        }, instanceId);
    }

    public static class TestGoal extends GoalSelector {

        public TestGoal(@NotNull EntityCreature entityCreature) {
            super(entityCreature);
        }

        @Override
        public boolean shouldStart() {
            return true;
        }

        @Override
        public void start() {
        }

        boolean direction = true;

        @Override
        public void tick(long time) {
            entityCreature.getNavigator().setPathTo(entityCreature.getPosition().withX(x -> {
                if (entityCreature.getPosition().blockX() > 35) {
                    direction = false;
                } else if (entityCreature.getPosition().blockX() < 12) {
                    direction = true;
                }

                return direction ? x + 1 : x - 1;
            }));
        }

        @Override
        public boolean shouldEnd() {
            var pos = entityCreature.getPosition();
            return false;
        }

        @Override
        public void end() {

        }
    }
}
