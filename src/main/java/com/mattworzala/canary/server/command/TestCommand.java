package com.mattworzala.canary.server.command;

import com.mattworzala.canary.server.command.test.BuilderCommand;
import com.mattworzala.canary.server.command.test.FilterCommand;
import com.mattworzala.canary.server.command.test.RunCommand;
import com.mattworzala.canary.platform.givemeahome.SandboxTestEnvironment;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.junit.platform.engine.TestDescriptor;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class TestCommand extends Command {
    private static final String VERSION = "1.2.3";

    public TestCommand() {
        super("test", "t");

        setDefaultExecutor(this::onHelp);   // Help (default)

        addSyntax(this::onList,             // List
                Literal("list"));
        addSyntax(this::onDiscover,         // Discover
                Literal("discover"));

        addSubcommand(new RunCommand());    // Run
        addSubcommand(new FilterCommand()); // Filter
        addSubcommand(new BuilderCommand());// Builder
    }

    private void onHelp(CommandSender sender, CommandContext context) {
        version(sender, null, VERSION);
        sender.sendMessage("Showing help menu...");
    }

    private void onList(CommandSender sender, CommandContext context) {
        version(sender, null, VERSION);

//        printTestRecursive(sender, env.getRoot(), -1);
    }

    /**
     *
     * For classes:
     *  - "TestDemo [filter] [run]
     *
     * @param sender
     * @param test
     * @param indent
     */
    private void printTestRecursive(CommandSender sender, TestDescriptor test, int indent) {
        if (indent != -1) {
            var indentComp = text(" ".repeat(indent));
            var nameComp = text(test.getDisplayName());

            var finalComp = indentComp.append(nameComp);
            sender.sendMessage(finalComp);
        }
        test.getChildren().forEach(child -> printTestRecursive(sender, child, indent + 1));
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

    public static void version(CommandSender sender, String name, String version) {
        var n = name == null ? "" : name + " ";
        var text = text("canary " + n + "v" + version)
                .color(NamedTextColor.GRAY);
        sender.sendMessage(text);
    }
}
