package com.mattworzala.canary.internal.util.ui;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.LeatherArmorMeta;

public interface ItemBehavior {

    default void onLeftClick(Player player, Point point) {
        // Do nothing
    }

    default void onRightClick(Player player, Point point) {
        // Do nothing
    }

    int getId();

    default ItemStack getItem() {
        return ItemStack.builder(Material.LEATHER_HORSE_ARMOR)
                .meta(LeatherArmorMeta.class, meta -> {
                    meta.customModelData(getId());
                })
                .build();
    }

}
