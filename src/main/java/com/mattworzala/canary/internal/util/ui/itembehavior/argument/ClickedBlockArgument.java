package com.mattworzala.canary.internal.util.ui.itembehavior.argument;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;

import java.util.concurrent.CompletableFuture;

public class ClickedBlockArgument extends Argument {
    @Override
    public CompletableFuture<String> get(Player player, Point point) {
        String str = point.blockX() + " " + point.blockY() + " " + point.blockZ();
        System.out.println("ClickedBlockArgument returning: " + str);
        return CompletableFuture.completedFuture(str);
    }
}
