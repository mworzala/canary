package com.mattworzala.canary.internal.util.ui.itembehavior.argument;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;

import java.util.concurrent.CompletableFuture;

public abstract class Argument {
    public abstract CompletableFuture<String> get(Player player, Point point);
}
