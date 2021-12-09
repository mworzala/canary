package com.example.extension.command;

import com.example.extension.minecart.BrokeEntity;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.instance.block.Block;
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

        for (int i = 0; i < 20; i++) {
            player.getInstance().setBlock(player.getPosition().add(0, 0, i), Block.RAIL);
        }

        var entity = new BrokeEntity();
        entity.setInstance(player.getInstance(), player.getPosition());
    }
}
