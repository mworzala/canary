package com.mattworzala.canary.server.command;

import com.mattworzala.canary.server.givemeahome.PlayerPacketRecorder;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;

public class InteractionCommand extends Command {
    private PlayerPacketRecorder recorder = null;

    public InteractionCommand() {
        super("interaction", "int");

        addSyntax(this::onStart, ArgumentType.Literal("start"));
        addSyntax(this::onStop, ArgumentType.Literal("stop"));
    }

    private void onStart(CommandSender sender, CommandContext context) {
        if (!sender.isPlayer()) return;

        sender.sendMessage("Starting recording");
        recorder = new PlayerPacketRecorder(sender.asPlayer());
    }

    private void onStop(CommandSender sender, CommandContext context) {
        if (recorder == null) return;

        recorder.stop();
        sender.sendMessage("Stopped recording.");
    }
}
