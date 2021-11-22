package com.mattworzala.canary.internal.server.sandbox.command.test.builder;

import com.mattworzala.canary.internal.server.sandbox.SandboxServer;
import com.mattworzala.canary.internal.server.sandbox.testbuilder.TestBuilderController;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class EditCommand extends Command {

    private SandboxServer server;

    CommandCondition inTestCondition;

    private ArgumentInteger xPos = ArgumentType.Integer("xPos");
    private ArgumentInteger yPos = ArgumentType.Integer("yPos");
    private ArgumentInteger zPos = ArgumentType.Integer("zPos");
    private ArgumentString markerName = ArgumentType.String("marker-name");

    public EditCommand(SandboxServer server) {
        super("edit", "e");
        this.server = server;

        inTestCondition = (sender, commandString) -> server.playerInTestBuilder(sender.asPlayer());

        addConditionalSyntax(inTestCondition, this::onMarker, Literal("marker"), xPos, yPos, zPos, markerName);
    }

    public void onMarker(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = sender.asPlayer();
        TestBuilderController testBuilder = server.getTestBuilderOfPlayer(player);

        if (testBuilder != null) {
            Point makerPos = getPointFromContext(context);
            String name = context.get(markerName);
            testBuilder.addMarker(makerPos, name);
            player.sendMessage("Made a marker with name " + name);
        }
    }

    private Point getPointFromContext(CommandContext context) {
        int x = context.get(xPos);
        int y = context.get(yPos);
        int z = context.get(zPos);
        return new Vec(x, y, z);
    }


}
