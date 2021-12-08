package com.mattworzala.canary.internal.util.ui.itembehavior.argument;

import net.minestom.server.item.ItemStack;

public class Arguments {
    public static Argument CLICKED_BLOCK = new ClickedBlockArgument();

    public static Argument StringPromptAnvil(String name, ItemStack cancelItem, ItemStack confirmItem) {
        return new AnvilStringPromptArgument(name, cancelItem, confirmItem);
    }

    public static Argument ChatResponsePrompt(String prompt) {
        return new ChatResponsePromptArgument(prompt);
    }
}
