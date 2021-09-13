package com.mattworzala.canary.server.command;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityCommand extends Command {
    public EntityCommand() {
        super("entity");

        addSyntax((sender, context) -> {
            var instance = sender.asPlayer().getInstance();

            EntityCreature entity = new EntityCreature(EntityType.ZOMBIE);
            entity.addAIGroup(new EntityAIGroupBuilder()
                    .addGoalSelector(new TestGoal(entity))
                    .build());
            entity.setInstance(instance, new Vec(12, 41, 22));
//            entity.getNavigator().setPathTo(new Vec(42, 41, 22), true);
        });
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
