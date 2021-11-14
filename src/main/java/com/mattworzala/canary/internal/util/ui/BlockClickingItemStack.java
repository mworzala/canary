package com.mattworzala.canary.internal.util.ui;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;

public class BlockClickingItemStack {

    //    private final ItemStack itemStack;
//    private final Function<Point, Boolean> leftClickHandler;
//    private final Function<Point, Boolean> rightClickHandler;
    ItemBehavior item;
    private EventListener<PlayerBlockBreakEvent> blockBreakEventEventListener;
    private EventListener<PlayerUseItemOnBlockEvent> useItemOnBlockEventEventListener;

    private static final EventNode<PlayerEvent> eventNode = EventNode.type("blockClickingItemStack", EventFilter.PLAYER);

    static {
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }

    public BlockClickingItemStack(ItemBehavior item) {
        this.item = item;
    }

    public void giveToPlayer(Player player, int slot) {
        ItemStack itemStack = this.item.getItem();
        ;
        player.getInventory().setItemStack(slot, itemStack);

        blockBreakEventEventListener =
                EventListener.builder(PlayerBlockBreakEvent.class)
                        .filter(event -> event.getPlayer().equals(player))
                        .filter(event -> {
                            Player p = event.getPlayer();
                            short heldSlot = p.getHeldSlot();
                            return p.getInventory().getItemStack(heldSlot).equals(itemStack);
                        })
                        .handler((event) -> {
                            this.item.onLeftClick(event.getPlayer(), event.getBlockPosition());
//                            if (leftClickHandler.apply(event.getBlockPosition())) {
//                                removeListenersFromEventNode();
//                                clearHeldItem(event.getPlayer());
//                            }
                            event.setCancelled(true);
                        }).build();

        useItemOnBlockEventEventListener = EventListener.builder(PlayerUseItemOnBlockEvent.class)
                .filter(event -> event.getPlayer().equals(player))
                .filter(event -> event.getItemStack().equals(itemStack))
                .handler((event) -> {
                    this.item.onRightClick(event.getPlayer(), event.getPosition());
//                    if (rightClickHandler.apply(event.getPosition())) {
//                        removeListenersFromEventNode();
//                        clearHeldItem(event.getPlayer());
//                    }
                }).build();

        eventNode.addListener(blockBreakEventEventListener);
        eventNode.addListener(useItemOnBlockEventEventListener);
    }

    private void clearHeldItem(Player player) {
        short heldSlot = player.getHeldSlot();
        player.getInventory().setItemStack(heldSlot, ItemStack.AIR);
    }

    private void removeListenersFromEventNode() {
        eventNode.removeListener(useItemOnBlockEventEventListener);
        eventNode.removeListener(blockBreakEventEventListener);
    }
}
