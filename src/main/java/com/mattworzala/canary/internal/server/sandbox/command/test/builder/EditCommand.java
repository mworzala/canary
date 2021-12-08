package com.mattworzala.canary.internal.server.sandbox.command.test.builder;

import com.mattworzala.canary.internal.server.sandbox.SandboxServer;
import com.mattworzala.canary.internal.server.sandbox.testbuilder.TestBuilderController;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class EditCommand extends Command {

    private SandboxServer server;

    CommandCondition inTestCondition;

    private ArgumentRelativeBlockPosition pos = ArgumentType.RelativeBlockPosition("pos");
    private ArgumentString markerName = ArgumentType.String("marker-name");
    private ArgumentString handlerName = ArgumentType.String("handler-name");

    public EditCommand(SandboxServer server) {
        super("edit", "e");
        this.server = server;

        inTestCondition = (sender, commandString) -> server.isPlayerInTestBuilder(sender.asPlayer());

        addConditionalSyntax(inTestCondition, this::onMarker, Literal("marker"), pos, markerName);
        addConditionalSyntax(inTestCondition, this::onHandler, Literal("handler"), pos, handlerName);
    }

    public void onMarker(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = sender.asPlayer();
        TestBuilderController testBuilder = server.getTestBuilderOfPlayer(player);

        if (testBuilder != null) {
            Point makerPos = context.get(pos).from(player);
            String name = context.get(markerName);
            testBuilder.addMarker(makerPos, name);
            player.sendMessage("Made a marker with name " + name);
        }
    }

    public void onHandler(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = sender.asPlayer();
        Instance instance = player.getInstance();
        String blockHandlerStr = context.get(handlerName);

        Point handlerPos = context.get(pos).from(player);
        BlockHandler blockHandler = MinecraftServer.getBlockManager().getHandlerOrDummy(blockHandlerStr);
        Block blockWithHandler = instance.getBlock(handlerPos).withHandler(blockHandler);
        instance.setBlock(handlerPos, blockWithHandler);
        player.sendMessage("Set handler for block at " + handlerPos + " to be " + blockHandler.toString());
    }
}
