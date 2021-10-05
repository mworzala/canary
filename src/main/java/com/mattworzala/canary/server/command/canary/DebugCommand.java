package com.mattworzala.canary.server.command.canary;

import com.mattworzala.canary.server.execution.CameraPlayer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class DebugCommand extends Command {

    private static final Map<String, BiConsumer<CommandSender, Object>> actions = new HashMap<>(){{
        put("sandbox.camera.forward", (sender, arg) -> {
            if (!(arg instanceof Boolean value)) {
                sender.sendMessage("Invalid argument. Expected boolean, found " + arg.getClass().getSimpleName());
                return;
            }
            CameraPlayer.DO_FORWARDING = value;
        });
        put("sandbox.camera.log", (sender, arg) -> {
            if (!(arg instanceof Boolean value)) {
                sender.sendMessage("Invalid argument. Expected boolean, found " + arg.getClass().getSimpleName());
                return;
            }
            CameraPlayer.DO_DEBUG_LOG = value;
        });
    }};

    private final ArgumentWord actionName = ArgumentType.Word("action")
            .from(actions.keySet().toArray(new String[0]));

    private final ArgumentBoolean argBool = ArgumentType.Boolean("arg-bool");
    private final ArgumentInteger argInt = ArgumentType.Integer("arg-int");

    public DebugCommand() {
        super("debug");

        setDefaultExecutor(this::onHelp);

        addSyntax(this::onArgBool, actionName, argBool);
        addSyntax(this::onArgInt, actionName, argInt);
    }

    private void onHelp(CommandSender sender, CommandContext context) {
        sender.sendMessage("Debug action help");
    }

    private void onArgBool(CommandSender sender, CommandContext context) {
        String actionName = context.get(this.actionName);
        Boolean arg = context.get(this.argBool);

        var action = actions.get(actionName);
        action.accept(sender, arg);
    }

    private void onArgInt(CommandSender sender, CommandContext context) {
        String actionName = context.get(this.actionName);
        Integer arg = context.get(this.argInt);

        var action = actions.get(actionName);
        action.accept(sender, arg);
    }
}
