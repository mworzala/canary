package com.example.extension.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class StandOnBlockGoal extends GoalSelector {
    private static final int range = 10;

    private final Block targetBlock;

    public StandOnBlockGoal(@NotNull EntityCreature entityCreature, @NotNull Block targetBlock) {
        super(entityCreature);

        this.targetBlock = targetBlock;
    }

    @Override
    public boolean shouldStart() {
        final Instance instance = entityCreature.getInstance();
        assert instance != null;

        final var posUnder = entityCreature.getPosition().withY(y -> y - 1);
        final var underEntity = instance.getBlock(posUnder);

        return !targetBlock.compare(underEntity);
    }

    @Override
    public void start() {
        final Instance instance = entityCreature.getInstance();
        assert instance != null;
        final Pos entityPos = entityCreature.getPosition();

        for (int x = -range; x < range; x++) {
            for (int y = -range; y < range; y++) {
                for (int z = -range; z < range; z++) {
                    final var offsetPos = entityPos.add(new Pos(x, y, z));
                    final var block = instance.getBlock(offsetPos);
                    if (targetBlock.compare(block)) {
                        var isValid = entityCreature.getNavigator().setPathTo(offsetPos, true);
                        if (isValid)
                            return;
                    }
                }
            }
        }


    }

    @Override
    public void tick(long time) {

    }

    @Override
    public boolean shouldEnd() {
        final Instance instance = entityCreature.getInstance();
        assert instance != null;

        final var posUnder = entityCreature.getPosition().withY(y -> y - 1);
        final var underEntity = instance.getBlock(posUnder);

        return targetBlock.compare(underEntity) || entityCreature.getNavigator().getPath() == null;
    }

    @Override
    public void end() {
        entityCreature.getNavigator().setPathTo(null);
    }
}
