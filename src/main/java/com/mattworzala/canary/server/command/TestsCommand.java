package com.mattworzala.canary.server.command;

import com.mattworzala.canary.test.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.test.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.test.sandbox.SandboxTestEnvironment;
import com.mattworzala.canary.test.sandbox.SandboxTestExecutor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestDescriptor;

/**
 * Shows information about all currently loaded tests.
 */
public class TestsCommand extends Command {

    public TestsCommand() {
        super("tests");

        setDefaultExecutor(this::onExecute);
    }

    private void onExecute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        sendTestsRecursive(sender, SandboxTestEnvironment.getInstance().getRoot(), 0);
    }

    private void sendTestsRecursive(CommandSender sender, TestDescriptor test, int indent) {
        sender.sendMessage(" ".repeat(indent) + test.getDisplayName());
        test.getChildren().forEach(child -> sendTestsRecursive(sender, child, indent + 1));
    }
}
