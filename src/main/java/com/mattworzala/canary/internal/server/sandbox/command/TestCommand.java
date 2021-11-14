package com.mattworzala.canary.internal.server.sandbox.command;

import com.mattworzala.canary.internal.execution.TestCoordinator;
import com.mattworzala.canary.internal.execution.TestExecutor;
import com.mattworzala.canary.internal.server.sandbox.SandboxServer;
import com.mattworzala.canary.internal.server.sandbox.command.test.BuilderCommand;
import com.mattworzala.canary.internal.server.sandbox.command.test.FilterCommand;
import com.mattworzala.canary.internal.server.sandbox.command.test.RunCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class TestCommand extends Command {
    private static final String VERSION = "1.2.3";

    private final SandboxServer server;

    public TestCommand(SandboxServer server) {
        super("test", "t");
        this.server = server;

        setDefaultExecutor(this::onHelp);   // Help (default)

        addSyntax(this::onList,             // List
                Literal("list"));
        addSyntax(this::onDiscover,         // Discover
                Literal("discover"));
        addSyntax(this::onTeleport,         // Teleport
                Literal("tp"), ArgumentType.StringArray("target"));

        addSubcommand(new RunCommand(server));  // Run
        addSubcommand(new FilterCommand());     // Filter
        addSubcommand(new BuilderCommand(server));    // Builder
    }

    private void onHelp(CommandSender sender, CommandContext context) {
        version(sender, null, VERSION);
        sender.sendMessage("Showing help menu...");
    }

    private void onList(CommandSender sender, CommandContext context) {
        version(sender, null, VERSION);

        TestCoordinator coordinator = server.getTestCoordinator();

        printTestRecursive(sender, coordinator.getEngineDescriptor(), -1);
    }

    private void onDiscover(CommandSender sender, CommandContext context) {
        version(sender, null, VERSION);

        sender.sendMessage(
                text("Discovering tests...")
                        .hoverEvent(showText(text("Test changes will automatically be applied when the source code is reloaded by the JVM.\n\nTest rediscovery is used to load new tests which did not exist when the server was started. This is only applicable to JVMs which supports hot loading methods and/or classes.")
                                .color(NamedTextColor.DARK_GRAY)))
        );

//        var result = env.discover();
//        sender.sendMessage(
//                text("Discovered ")
//                .append(text(result.tests()))
//                .append(text(" tests in "))
//                .append(text(result.files()))
//                .append(text(" files in "))
//                .append(text(result.packages()))
//                .append(text(" packages."))
//        );
    }

    private void onTeleport(CommandSender sender, CommandContext context) {
        if (!sender.isPlayer()) {
            sender.sendMessage("Players only..."); //todo
            return;
        }

        version(sender, null, VERSION);

        String[] targetParts = context.get("target");
        String targetStr = String.join(" ", targetParts);
        UniqueId target = UniqueId.parse(targetStr);

        TestExecutor executor = server.getTestCoordinator().getExecutor(target);
        if (executor == null) {
            sender.sendMessage(text("Unknown test.").color(NamedTextColor.RED));
            return;
        }

        Player player = sender.asPlayer();
        if (executor.getInstance().equals(player.getInstance())) {
            player.teleport(new Pos(executor.getOrigin()));
        } else {
            player.setInstance(executor.getInstance(), new Pos(executor.getOrigin()));
        }
    }

    public static void version(CommandSender sender, String name, String version) {
        var n = name == null ? "" : name + " ";
        var text = text("canary " + n + "v" + version)
                .color(NamedTextColor.GRAY);
        sender.sendMessage(text);
    }

    /**
     * For classes:
     * - "TestDemo [filter] [run]
     *
     * @param sender
     * @param test
     * @param indent
     */
    private void printTestRecursive(CommandSender sender, TestDescriptor test, int indent) {
        if (indent != -1) {
            var indentComp = text(" ".repeat(indent));
            var nameComp = text(test.getDisplayName());

            Component finalComp = indentComp.append(nameComp);
            if (test.getSource().isPresent()) {
                finalComp = finalComp.append(text(" [visit]")
                        .color(NamedTextColor.GRAY)
                        .clickEvent(ClickEvent.runCommand("/test tp " + test.getUniqueId().toString())));
            }
            sender.sendMessage(finalComp);
        }
        test.getChildren().forEach(child -> printTestRecursive(sender, child, indent + 1));
    }
}
