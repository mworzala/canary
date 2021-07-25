package com.example.extension.command;

import com.example.extension.entity.TestEntity;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public class EntityCommand extends Command {
    public EntityCommand() {
        super("entity");

        setDefaultExecutor(this::onExecute);
    }

    private void onExecute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!sender.isPlayer()) return;

        var player = sender.asPlayer();
        player.sendMessage("Summoning test entity.");

        TestEntity entity = new TestEntity();
        entity.setInstance(player.getInstance(), player.getPosition().add(2, 0, 0));
    }
}
