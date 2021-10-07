package com.mattworzala.canary.server.givemeahome;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DesyncedTicker implements Runnable {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final List<Tickable> ticking = new CopyOnWriteArrayList<>();

    public DesyncedTicker(long time, TimeUnit unit) {
        executor.scheduleAtFixedRate(this, time, time, unit);
    }

    public void add(@NotNull Tickable tickable) {
        ticking.add(tickable);
    }

    public void remove(@NotNull Tickable tickable) {
        ticking.remove(tickable);
    }

    @Override
    public void run() {
        ticking.forEach(tickable -> tickable.tick(-1));
    }
}
