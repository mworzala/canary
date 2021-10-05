package com.mattworzala.canary.server.command.test;

import com.mattworzala.canary.platform.TestExecutionListener;
import com.mattworzala.canary.server.SandboxServer;
import com.mattworzala.canary.server.execution.TestCoordinator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.engine.TestDescriptor;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.mattworzala.canary.server.command.TestCommand.version;
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

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            TestCoordinator coordinator = server.getTestCoordinator();
            System.out.println("STARTING EXECUTION");
            coordinator.execute(TestExecutionListener.STDOUT);
            System.out.println("EXECUTION FINISHED");
        }).schedule();

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
