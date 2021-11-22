package com.mattworzala.canary.internal.util.ui.itembehavior;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;

public class GenericItemBehavior implements ItemBehavior {
    private String baseCommand;
    private CommandClick leftClick;
    private CommandClick rightClick;

    public GenericItemBehavior(String baseCommand, CommandClick leftClick, CommandClick rightClick) {
        this.baseCommand = baseCommand;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
    }

    @Override
    public void onLeftClick(Player player, Point point) {
        if (leftClick != null) {
            try {
                String argumentsStr = leftClick.handle(player, point);
                MinecraftServer.getCommandManager().execute(player, baseCommand + " " + argumentsStr);
            } catch (Exception ignored) {
            }
        }

    }

    @Override
    public void onRightClick(Player player, Point point) {
        if (rightClick != null) {
            try {
                String argumentsStr = rightClick.handle(player, point);
                MinecraftServer.getCommandManager().execute(player, baseCommand + " " + argumentsStr);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public int getId() {
        return 0;
    }
}
