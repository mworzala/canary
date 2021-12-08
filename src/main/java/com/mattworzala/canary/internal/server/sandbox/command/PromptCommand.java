package com.mattworzala.canary.internal.server.sandbox.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.ArgumentType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PromptCommand extends Command {

    private static PromptCommand instance;

    private final int KEY_LENGTH = 6;

    public static PromptCommand getInstance() {
        if (instance == null) {
            instance = new PromptCommand();
        }
        return instance;
    }

    private final Map<String, CommandExecutor> registeredPrompts = new HashMap<>();
//    private final List<CommandExecutor> registeredPrompts = new ArrayList<>();

    public PromptCommand() {
        super("prompt");

//        addSyntax((((sender, context) -> {
//            Player player = sender.asPlayer();
//            var itemStack = ItemStack.builder(Material.BOOK)
//                    .displayName(Component.text("Test Builder", NamedTextColor.GREEN))
//                    .build();
//
//            AtomicInteger count = new AtomicInteger();
//
//            Function<Point, Boolean> onLeftClick = (Point p) -> {
//                System.out.println("left click: " + p);
//                int c = count.getAndIncrement() + 1;
//                if (c >= 3) {
//                    sender.sendMessage("YOU FINISHED!");
//                    return true;
//                }
//                return false;
//            };
//            Function<Point, Boolean> onRightClick = (Point p) -> {
//                System.out.println("right click: " + p);
//                int c = count.getAndIncrement() + 1;
//                if (c >= 3) {
//                    sender.sendMessage("YOU FINISHED!");
//                    return true;
//                }
//                return false;
//            };
//            BlockClickingItemStack blockClickingItemStack = new BlockClickingItemStack(itemStack, onLeftClick, onRightClick);
//            blockClickingItemStack.giveToPlayer(player, player.getHeldSlot());
//        })), Literal("item-test"));
//
//        addSyntax((((sender, context) -> {
//            List<Prompt.ChatPromptOption> options = new ArrayList<>(3);
//            options.add(new Prompt.ChatPromptOption("a", () -> System.out.println("a pressed!")));
//            options.add(new Prompt.ChatPromptOption("b", () -> System.out.println("b pressed!")));
//            options.add(new Prompt.ChatPromptOption("c", () -> System.out.println("c pressed!")));
//
////            var leftItem = ItemStack.builder(Material.RED_STAINED_GLASS).displayName(Component.text("")).lore(Component.text("cancel")).build();
////            var rightItem = ItemStack.builder(Material.GREEN_STAINED_GLASS).build();
////            var lhs = new Prompt.AnvilPromptOption(leftItem, s -> System.out.println("lhs clicked: " + s));
////            var rhs = new Prompt.AnvilPromptOption(rightItem, s -> System.out.println("rhs clicked: " + s));
//            Prompt.chatPrompt(sender.asPlayer(), "test prompt wahoo", options);
////            Prompt.anvilPrompt(sender.asPlayer(), "Anvil Prompt", lhs, rhs);
//        })), Literal("test"));
//
        addSyntax((((sender, context) -> {
            String key = context.get("key");
            CommandExecutor prompt = registeredPrompts.get(key);
            if (prompt != null) {
                prompt.apply(sender, context);
            } else {
                if (registeredPrompts.containsKey(key)) {
                    System.out.println("The registered command for key " + key + " was null");
                } else {
                    System.out.println("There is no prompt registered for key " + key);
                }
            }
        })), ArgumentType.String("key"));
    }

    public String registerCommand(CommandExecutor callback) {
        String key = getNewPromptKey();
        registeredPrompts.put(key, callback);
        System.out.println("registered a prompt with key" + key);

        return "/prompt " + key;
    }

    private String getNewPromptKey() {
        String key = randomStringOfLength(KEY_LENGTH);
        while (registeredPrompts.containsKey(key)) {
            key = randomStringOfLength(KEY_LENGTH);
        }
        return key;
    }

    /**
     * @param length Number of characters in returned string
     * @return A random string of alphanumeric characters of the given length
     */
    private String randomStringOfLength(int length) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < length; i++) {
            res.append(randomAlphaNumeric());
        }
        return res.toString();
    }

    /**
     * @return A random alphanumeric character [0-9a-zA-Z]
     */
    private char randomAlphaNumeric() {
        final int numPossibleChars = 10 + 26 + 26;
        Random r = new Random();
        int v = r.nextInt(numPossibleChars);
        if (v < 10) {
            return (char) (48 + v); // 48 is the ascii code for '0'
        }
        if (v < 10 + 26) {
            return (char) (97 + v - 10); // 97 is the ascii code for 'a'
        }
        return (char) (65 + v - 36); // 65 is the ascii code for 'A'
    }

    public void unregisterCommand(String key) {
        registeredPrompts.remove(key);
    }


}
