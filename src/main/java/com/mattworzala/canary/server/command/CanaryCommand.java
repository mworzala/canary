package com.mattworzala.canary.server.command;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;

public class CanaryCommand extends Command {
    public CanaryCommand() {
        super("canary");

        setDefaultExecutor(this::onHelp);
    }

    private void onHelp(CommandSender sender, CommandContext context) {
        sender.sendMessage("/canary help");
    }
}
