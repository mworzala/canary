package com.mattworzala.canary.server.command;

import com.mattworzala.canary.server.ui.BlockClickingItemStack;
import com.mattworzala.canary.server.ui.Prompt;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class PromptCommand extends Command {

    private static PromptCommand instance;

    public static PromptCommand getInstance() {
        if (instance == null) {
            instance = new PromptCommand();
        }
        return instance;
    }

    private final List<CommandExecutor> registeredPrompts = new ArrayList<>();

    public PromptCommand() {
        super("prompt");

        addSyntax((((sender, context) -> {
            Player player = sender.asPlayer();
            var itemStack = ItemStack.builder(Material.BOOK)
                    .displayName(Component.text("Test Builder", NamedTextColor.GREEN))
                    .build();

            AtomicInteger count = new AtomicInteger();

            Function<Point, Boolean> onLeftClick = (Point p) -> {
                System.out.println("left click: " + p);
                int c = count.getAndIncrement() + 1;
                if (c >= 3) {
                    sender.sendMessage("YOU FINISHED!");
                    return true;
                }
                return false;
            };
            Function<Point, Boolean> onRightClick = (Point p) -> {
                System.out.println("right click: " + p);
                int c = count.getAndIncrement() + 1;
                if (c >= 3) {
                    sender.sendMessage("YOU FINISHED!");
                    return true;
                }
                return false;
            };
            BlockClickingItemStack blockClickingItemStack = new BlockClickingItemStack(itemStack, onLeftClick, onRightClick);
            blockClickingItemStack.giveToPlayer(player, player.getHeldSlot());
        })), Literal("item-test"));

        addSyntax((((sender, context) -> {
//            List<Prompt.ChatPromptOption> options = new ArrayList<>(3);
            var leftItem = ItemStack.builder(Material.RED_STAINED_GLASS).displayName(Component.text("")).lore(Component.text("cancel")).build();
            var rightItem = ItemStack.builder(Material.GREEN_STAINED_GLASS).build();
            var lhs = new Prompt.AnvilPromptOption(leftItem, s -> System.out.println("lhs clicked: " + s));
            var rhs = new Prompt.AnvilPromptOption(rightItem, s -> System.out.println("rhs clicked: " + s));
//            Prompt.chatPrompt(sender.asPlayer(), "test prompt wahoo", options);
            Prompt.anvilPrompt(sender.asPlayer(), "Anvil Prompt", lhs, rhs);
        })), Literal("test"));

        addSyntax((((sender, context) -> {
            int index = context.get("index");
            if (index < registeredPrompts.size()) {
                var prompt = registeredPrompts.get(index);
                if (prompt != null) {
                    prompt.apply(sender, context);
                } else {
                    System.out.println("prompt at index " + index + " has been unregistered");
                }
            } else {
                System.out.println("index " + index + " out of range of registered prompts (size=" + registeredPrompts.size() + ")");
            }
        })), ArgumentType.Integer("index"));
    }

    public String registerCommand(CommandExecutor callback) {
        String index = registeredPrompts.size() + "";
        registeredPrompts.add(callback);
        System.out.println("registered a prompt at index " + index);

//        addSyntax(((s, c) -> {
//            System.out.println("received command that is a prompt index");
//        }), Literal(index));
        return "/prompt " + index;
    }

    public void unregisterCommand(int index) {
        registeredPrompts.set(index, null);
    }


}
