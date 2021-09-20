package com.mattworzala.canary.server.command.test;

import com.mattworzala.canary.platform.givemeahome.TestExecutionListener;
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

    public RunCommand() {
        super("run", "r", "execute", "exec");

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
            TestCoordinator coordinator = SandboxServer.getInstance().getTestCoordinator();
            System.out.println("STARTING EXECUTION");
            coordinator.execute(new TestExecutionListener() {
                @Override
                public void start(@NotNull TestDescriptor descriptor) {
                    sender.sendMessage(Component.text(" ".repeat(indent) + "+" + descriptor.getDisplayName()).color(NamedTextColor.GREEN));
                    indent += 1;
                }

                @Override
                public void end(@NotNull TestDescriptor descriptor, @Nullable Throwable failure) {
                    indent -= 1;
                    sender.sendMessage(Component.text(" ".repeat(indent) + "-" + descriptor.getDisplayName()).color(failure == null ? NamedTextColor.GRAY : NamedTextColor.RED));
                    if (failure != null) {
                        System.out.println("TEST FAILED: " + descriptor.getUniqueId());
                        failure.printStackTrace();
                    }
                }
            });
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
