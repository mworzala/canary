package com.mattworzala.canary.internal.util.ui;

import com.mattworzala.canary.internal.server.sandbox.command.PromptCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.inventory.type.AnvilInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientNameItemPacket;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Different ways to prompt the user for information
 */
public class Prompt {
    public record ChatPromptOption(String text, Runnable callback) {
    }

    public static final int ANVIL_LHS_SLOT = 0;
    public static final int ANVIL_RHS_SLOT = 2;

    public record AnvilPromptOption(ItemStack item, Consumer<String> callback) {
    }

    ;

    /**
     * Gives the player a main prompt and then a list of clickable options
     * As it currently stands, no attempt is made to prevent the player from selecting multiple prompts
     *
     * @param player        The player to send the prompt to
     * @param prompt        First text to show the player
     * @param promptOptions All the options to show the player
     */
    public static void chatPrompt(Player player, String prompt, List<ChatPromptOption> promptOptions) {
        player.sendMessage(Component.text(prompt));
        PromptCommand promptCommand = PromptCommand.getInstance();
        for (ChatPromptOption option : promptOptions) {

            String command = promptCommand.registerCommand((s, c) -> {
                option.callback.run();
            });

            System.out.println("chat prompt registered a command \"" + command + "\"");
            final TextComponent textOption = Component.text(option.text)
                    .clickEvent(ClickEvent.runCommand(command));

            player.sendMessage(textOption);
        }
    }

    /**
     * Shows the player an anvil inventory with the given title
     * The player can click in the left slot to cancel, or the right slot to "confirm"
     * Closing the inventory is also considered canceling
     * Both the cancel callback and the right-hand callback get given whatever text the player has entered
     *
     * @param player
     * @param title
     * @param cancelOption
     * @param rightHandOption
     */
    public static void anvilPrompt(Player player, String title, AnvilPromptOption cancelOption, AnvilPromptOption rightHandOption) {
        AnvilInventory anvilInventory = new AnvilInventory(title);
        anvilInventory.setItemStack(ANVIL_LHS_SLOT, cancelOption.item);
        anvilInventory.setItemStack(ANVIL_RHS_SLOT, rightHandOption.item);

        // Minestom does not currently handle NameItem packets, so we set the listener
        // TODO - do this using events
        AtomicReference<String> name = new AtomicReference<>("");
        MinecraftServer.getPacketListenerManager().setListener(ClientNameItemPacket.class, (packet, player1) -> {
            name.set(packet.itemName());
//            anvilInventory.setItemStack(ANVIL_RHS_SLOT, rightHandOption.item);
//            anvilInventory.setRepairCost((short) 0);
        });

        anvilInventory.addInventoryCondition((p, slot, clickType, inventoryConditionResult) -> {
            if (slot == ANVIL_LHS_SLOT) {
                cancelOption.callback.accept(name.get());
                p.closeInventory();
            }
            if (slot == ANVIL_RHS_SLOT) {
                rightHandOption.callback.accept(name.get());
                p.closeInventory();
            }
            inventoryConditionResult.setCancel(true);
        });
        player.openInventory(anvilInventory);

        // listen for the player closing the inventory to cancel
        EventNode<InventoryEvent> inventoryNode = EventNode.type("inventory-listener", EventFilter.INVENTORY);

        inventoryNode.addListener(EventListener.builder(InventoryCloseEvent.class)
                .handler(inventoryCloseEvent -> {
                    if (inventoryCloseEvent.getPlayer().getUuid().equals(player.getUuid()) &&
                            inventoryCloseEvent.getInventory().equals(anvilInventory)) {
                        cancelOption.callback.accept(name.get());
                    }
                }).build());

        MinecraftServer.getGlobalEventHandler().addChild(inventoryNode);
    }
}
