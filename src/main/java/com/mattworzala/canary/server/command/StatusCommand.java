package com.mattworzala.canary.server.command;

import net.minestom.server.command.builder.Command;

/**
 * Shows information about currently executing tests
 */
public class StatusCommand extends Command {
    public StatusCommand() {
        super("status");
    }
}
