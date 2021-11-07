package com.mattworzala.canary.internal.server.sandbox.command.test;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;

import static com.mattworzala.canary.internal.server.sandbox.command.TestCommand.version;
import static com.mattworzala.canary.internal.server.sandbox.command.test.RunCommand.NAME;
import static com.mattworzala.canary.internal.server.sandbox.command.test.RunCommand.VERSION;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class FilterCommand extends Command {
    // Subset of RunCommand

//    private final Argument<String> packageArgument = Word("package")
//            .setSuggestionCallback((sender, context, suggestion) -> {
//                //todo can i filter before sending suggestions?
//                System.out.println("SUGGEST PACKAGE");
//                env.getTestPackages()
//                        .map(SuggestionEntry::new)
//                        .forEach(suggestion::addEntry);
//            });

//    private final Argument<String> fileArgument = Word("file")
//            .setSuggestionCallback((sender, context, suggestion) -> {
//                //todo need to find out which ones are duplicated and differentiate them
//                env.getTestFiles()
//                        .map(file -> new SuggestionEntry(
//                                file.substring(file.lastIndexOf(".") + 1),
//                                Component.text(file)))
//                        .forEach(suggestion::addEntry);
//            });

    //todo parse this better
    private final Argument<String> regexArgument = String("regex");

    public FilterCommand() {
        super("filter", "f");

        setDefaultExecutor(this::onHelp);               // Help

        //todo rework to allow multiple commands, eg `clear package ... file ...`

        addSyntax(this::onList, Literal("list"));   // List
        addSyntax(this::onClear, Literal("clear")); // Clear

//        addSyntax(this::onTypePackage,                  // Package
//                Literal("package"), packageArgument);
//        addSyntax(this::onTypeFile,                     // File
//                Literal("file"), fileArgument);
        addSyntax(this::onTypeRegex,                    // Regex
                Literal("regex"), regexArgument);
    }

    private void onHelp(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);

        sender.sendMessage("Filter help");
    }

    private void onList(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);


        sender.sendMessage("Filter list");
    }

    private void onClear(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);

        sender.sendMessage("Filter clear");
    }

    private void onTypePackage(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);

//        String packageName = context.get(packageArgument);
//        sender.sendMessage("filtered to " + packageName);
    }

    private void onTypeFile(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);

//        String fileName = context.get(fileArgument);
//        sender.sendMessage("filtered to file: " + fileName);
    }

    private void onTypeRegex(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);

        String regexRaw = context.get(regexArgument);
        sender.sendMessage("filtered with regex: " + regexRaw);
    }
}
