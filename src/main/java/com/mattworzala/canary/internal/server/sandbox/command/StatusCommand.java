package com.mattworzala.canary.internal.server.sandbox.command;

import net.minestom.server.command.builder.Command;

/**
 * Shows information about currently executing tests
 */
public class StatusCommand extends Command {
    public StatusCommand() {
        super("status");
    }
}
