package com.mattworzala.canary.internal.server.sandbox.testbuilder;

import com.mattworzala.canary.internal.util.ui.Prompt;
import com.mattworzala.canary.internal.util.ui.itembehavior.ItemBehavior;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class MarkerItem implements ItemBehavior {
    private final TestBuilderController controller;

    public MarkerItem(TestBuilderController controller) {
        this.controller = controller;
    }

    @Override
    public void onLeftClick(Player player, Point point) {
        ItemStack leftItem = ItemStack.builder(Material.RED_STAINED_GLASS).displayName(Component.text("")).lore(Component.text("cancel")).build();
        ItemStack rightItem = ItemStack.builder(Material.GREEN_STAINED_GLASS).build();
        System.out.println("onLeftClick thread is: " + Thread.currentThread().getName());
        Prompt.AnvilPromptOption cancel = new Prompt.AnvilPromptOption(leftItem, null);
        Prompt.AnvilPromptOption confirm = new Prompt.AnvilPromptOption(rightItem, name -> {
            System.out.println("confirm thread is: " + Thread.currentThread().getName());
            StringBuilder commandBuilder = new StringBuilder("test builder edit marker ");
            commandBuilder.append(point.blockX());
            commandBuilder.append(" ");
            commandBuilder.append(point.blockY());
            commandBuilder.append(" ");
            commandBuilder.append(point.blockZ());
            commandBuilder.append(" ");
            commandBuilder.append(name);
            MinecraftServer.getCommandManager().execute(player, commandBuilder.toString());
        });
        Prompt.anvilPrompt(player, "Marker Name", cancel, confirm);
    }

    @Override
    public int getId() {
        return 1235;
    }
}
