package com.mattworzala.canary.internal.util.ui.itembehavior;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.LeatherArmorMeta;

public class GenericItemBehavior implements ItemBehavior {
    private String baseCommand;
    private CommandClick leftClick;
    private CommandClick rightClick;
    private final ItemStack DEFAULT_ITEM_STACK = ItemStack.builder(Material.LEATHER_HORSE_ARMOR)
            .meta(LeatherArmorMeta.class, meta -> {
                meta.customModelData(getId());
            })
            .build();
    private ItemStack itemStack;

    public GenericItemBehavior(String baseCommand, CommandClick leftClick, CommandClick rightClick) {
        this.baseCommand = baseCommand;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.itemStack = DEFAULT_ITEM_STACK;
    }

    public GenericItemBehavior(String baseCommand, CommandClick leftClick, CommandClick rightClick, ItemStack itemStack) {
        this.baseCommand = baseCommand;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.itemStack = itemStack;
    }

    @Override
    public void onLeftClick(Player player, Point point) {
        if (leftClick != null) {
            try {
                leftClick.handle(player, point)
                        .thenAccept(str -> {
                            if (str != null) {
                                MinecraftServer.getCommandManager().execute(player, baseCommand + " " + str);
                            }
                        });
            } catch (Exception e) {
                System.out.println("exception happened in onLeftClick: " + e);
            }
        }

    }

    @Override
    public void onRightClick(Player player, Point point) {
        if (rightClick != null) {
            try {
                rightClick.handle(player, point)
                        .thenAccept(str -> {
                            if (str != null) {
                                MinecraftServer.getCommandManager().execute(player, baseCommand + " " + str);
                            }
                        });
            } catch (Exception e) {
                System.out.println("exception happened in onRightClick: " + e);
            }
        }
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public ItemStack getItem() {
        return itemStack;
    }
}
