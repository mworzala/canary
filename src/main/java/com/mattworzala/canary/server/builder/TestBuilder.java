package com.mattworzala.canary.server.builder;

import com.mattworzala.canary.server.builder.instance.TestBuilderInstance;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestBuilder implements Viewable, Tickable {
    private final Set<Player> viewers = new HashSet<>();
    private final Instance instance = new TestBuilderInstance(UUID.randomUUID(), TestBuilderInstance.DIMENSION_TYPE);

    @Override
    public void tick(long time) {
        //todo resend expired markers, ...
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        if (isViewer(player)) return false;


        viewers.add(player);
        return true;
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        if (!isViewer(player)) return false;

        viewers.remove(player);
        return true;
    }

    @Override
    public @NotNull Set<Player> getViewers() {
        return viewers;
    }
}
