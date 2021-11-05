package com.mattworzala.canary.server.ui;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;

import java.util.function.Function;

public class BlockClickingItemStack {

    private ItemStack itemStack;
    private Function<Point, Boolean> leftClickHandler;
    private Function<Point, Boolean> rightClickHandler;
    private EventListener<PlayerBlockBreakEvent> blockBreakEventEventListener;
    private EventListener<PlayerUseItemOnBlockEvent> useItemOnBlockEventEventListener;

    private static EventNode<PlayerEvent> eventNode = EventNode.type("blockClickingItemStack", EventFilter.PLAYER);

    static {
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }

    public BlockClickingItemStack(ItemStack itemStack, Function<Point, Boolean> leftHandle, Function<Point, Boolean> rightHandler) {
        this.itemStack = itemStack;
        this.leftClickHandler = leftHandle;
        this.rightClickHandler = rightHandler;
    }

    public void giveToPlayer(Player player, int slot) {
        player.getInventory().setItemStack(slot, this.itemStack);

        blockBreakEventEventListener =
                EventListener.builder(PlayerBlockBreakEvent.class)
                        .filter(event -> event.getPlayer().equals(player))
                        .filter(event -> {
                            Player p = event.getPlayer();
                            short heldSlot = p.getHeldSlot();
                            return p.getInventory().getItemStack(heldSlot).equals(this.itemStack);
                        })
                        .handler((event) -> {
                            if (leftClickHandler.apply(event.getBlockPosition())) {
                                removeListenersFromEventNode();
                                clearHeldItem(event.getPlayer());
                            }
                            event.setCancelled(true);
                        }).build();

        useItemOnBlockEventEventListener = EventListener.builder(PlayerUseItemOnBlockEvent.class)
                .filter(event -> event.getPlayer().equals(player))
                .filter(event -> event.getItemStack().equals(this.itemStack))
                .handler((event) -> {
                    if (rightClickHandler.apply(event.getPosition())) {
                        removeListenersFromEventNode();
                        clearHeldItem(event.getPlayer());
                    }
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
