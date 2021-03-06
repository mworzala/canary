package com.mattworzala.canary.internal.server.sandbox.command.test;

import com.mattworzala.canary.internal.execution.TestExecutionListener;
import com.mattworzala.canary.internal.server.sandbox.SandboxServer;
import com.mattworzala.canary.internal.execution.TestCoordinator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.mattworzala.canary.internal.server.sandbox.command.TestCommand.version;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class RunCommand extends Command {
    public static final String NAME = "run";
    public static final String VERSION = "0.0.1";

    private enum RunFilter {
        ALL, PASSED, FAILED
    }

    private final SandboxServer server;

    public RunCommand(SandboxServer server) {
        super("run", "r", "execute", "exec");
        this.server = server;

        // No filter
        setDefaultExecutor(this::onRun);

        // Filter
        var filter = Enum("filter", RunFilter.class);
        filter.setFormat(ArgumentEnum.Format.LOWER_CASED);
        filter.setCallback(this::onIncorrectFilter);
        addSyntax(this::onRun, filter);
    }

    int indent = 0;

    private void onRun(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);

        new Thread(() -> {
            TestCoordinator coordinator = server.getTestCoordinator();
            System.out.println("STARTING EXECUTION");
            coordinator.execute(TestExecutionListener.STDOUT);
            System.out.println("EXECUTION FINISHED");
        }).start();

//        RunFilter runFilter = context.get("filter");
//        if (runFilter == null) runFilter = RunFilter.ALL;
//
//        sender.sendMessage(runFilter.name());
    }

    private void onIncorrectFilter(CommandSender sender, ArgumentSyntaxException exception) {
        var okValues = Arrays.stream(RunFilter.values())
                .map(RunFilter::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining(", "));
        sender.sendMessage("The acceptable values for run type are: " + okValues);
    }
}
