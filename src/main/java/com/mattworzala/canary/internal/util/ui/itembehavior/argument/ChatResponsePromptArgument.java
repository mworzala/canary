package com.mattworzala.canary.internal.util.ui.itembehavior.argument;

import com.mattworzala.canary.internal.util.ui.Prompt;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;

import java.util.concurrent.CompletableFuture;

public class ChatResponsePromptArgument extends Argument {
    private String prompt;

    public ChatResponsePromptArgument(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public CompletableFuture<String> get(Player player, Point point) {
        CompletableFuture<String> cf = new CompletableFuture<>();

        Prompt.chatResponsePrompt(player, Component.text(prompt), handlerStr -> {
            if (handlerStr == null) {
                player.sendMessage("Canceled!");
                cf.completeExceptionally(new Exception("canceled chat response prompt"));
            }
            cf.complete(handlerStr);
        });
        return cf;
    }
}
