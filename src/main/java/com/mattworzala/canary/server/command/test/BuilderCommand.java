package com.mattworzala.canary.server.command.test;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockGetter;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.mattworzala.canary.server.command.TestCommand.version;

public class BuilderCommand extends Command {
    private static final String NAME = "builder";
    private static final String VERSION = "0.0.1";

    record BlockDef(int blockId, int blockCount) {
    }

    public BuilderCommand() {
        super("builder", "b");

        setDefaultExecutor(this::onBuild);

    }

    private void onBuild(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        Player player = commandSender.asPlayer();
        final var playerPos = player.getPosition();
        final var playerInstance = player.getInstance();
        final int sizeX = 4;
        final int sizeY = 4;
        final int sizeZ = 4;

        Set<Block> blockSet = new HashSet<>();
        blockSet.add(Block.AIR);
        Map<Integer, Block> blockMap = new HashMap<>();

        blockMap.put(-1, Block.AIR);
        int blockMapIndex = 0;
        Block lastBlock = null;
        int lastBlockIndex = -1;
        int currentBlockCount = 0;
        List<BlockDef> blockDefList = new ArrayList<>();
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    Block b = playerInstance.getBlock(playerPos.blockX() + x, playerPos.blockY() + y, playerPos.blockZ() + z, BlockGetter.Condition.NONE);
                    System.out.println(b);
                    // if this is the very first block
                    if (lastBlock == null) {
                        if (blockSet.add(b)) {
                            // if this is a new block we haven't seen before
                            // put it in the block map
                            blockMap.put(blockMapIndex, b);
                            blockMapIndex++;
                            lastBlockIndex = 0;

                            lastBlock = b;
                        } else {
                            lastBlock = b;
                            lastBlockIndex = -1;
                        }
                        currentBlockCount++;
                    } else {
                        if (b.equals(lastBlock)) {
                            currentBlockCount++;
                        } else {
                            blockDefList.add(new BlockDef(lastBlockIndex, currentBlockCount));

                            if (blockSet.add(b)) {
                                // if this is a new block we haven't seen before
                                // put it in the block map
                                blockMap.put(blockMapIndex, b);
                                lastBlockIndex = blockMapIndex;
                                blockMapIndex++;

                                lastBlock = b;
                            } else {
                                lastBlock = b;
                                for (int key : blockMap.keySet()) {
                                    if (blockMap.get(key).equals(b)) {
                                        lastBlockIndex = key;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        blockDefList.add(new BlockDef(lastBlockIndex, currentBlockCount));

        for (int key : blockMap.keySet()) {
            System.out.println(key + ": " + blockMap.get(key));
        }
        System.out.println(blockDefList.stream().map((blockDef) -> blockDef.blockId + "," + blockDef.blockCount).collect(Collectors.joining(";")));
    }

    private void onHelp(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);

        sender.sendMessage("Test builder help...");
    }
}

class DemoHandler implements BlockHandler {
    @Override
    public void onPlace(@NotNull Placement placement) {
        if (placement instanceof PlayerPlacement) {
            // A player placed the block
        }
        Block block = placement.getBlock();
        System.out.println("The block " + block.name() + " has been placed");
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        // Namespace required for serialization purpose
        return NamespaceID.from("minestom:demo");
    }
}
