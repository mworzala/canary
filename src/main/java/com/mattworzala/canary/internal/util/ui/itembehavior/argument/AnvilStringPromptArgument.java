package com.mattworzala.canary.internal.util.ui.itembehavior.argument;

import com.mattworzala.canary.internal.util.ui.Prompt;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.util.concurrent.CompletableFuture;

public class AnvilStringPromptArgument extends Argument {

    private String prompt;
    private ItemStack leftItem;
    private ItemStack rightItem;

    public AnvilStringPromptArgument(String prompt, ItemStack leftItem, ItemStack rightItem) {
        this.prompt = prompt;
        this.leftItem = leftItem;
        this.rightItem = rightItem;
    }

    @Override
    public CompletableFuture<String> get(Player player, Point point) {
        CompletableFuture<String> cf = new CompletableFuture<>();
        Prompt.AnvilPromptOption cancel = new Prompt.AnvilPromptOption(leftItem, s -> {
            cf.completeExceptionally(new Exception("player canceled anvil prompt"));
        });
        Prompt.AnvilPromptOption confirm = new Prompt.AnvilPromptOption(rightItem, cf::complete);
        Prompt.anvilPrompt(player, prompt, cancel, confirm);
        return cf;
    }


}
