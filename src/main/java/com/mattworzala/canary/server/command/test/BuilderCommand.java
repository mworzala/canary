package com.mattworzala.canary.server.command.test;

import com.mattworzala.canary.server.givemeahome.JsonStructureIO;
import com.mattworzala.canary.server.givemeahome.Structure;
import com.mattworzala.canary.server.givemeahome.StructureWriter;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockGetter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.mattworzala.canary.server.command.TestCommand.version;

public class BuilderCommand extends Command {
    private static final String NAME = "builder";
    private static final String VERSION = "0.0.1";

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

        Structure resultStructure = new Structure("testStruct", sizeX, sizeY, sizeZ);

        Set<Block> blockSet = new HashSet<>();
        blockSet.add(Block.AIR);
        Map<Integer, Block> blockMap = new HashMap<>();

        blockMap.put(-1, Block.AIR);
        int blockMapIndex = 0;
        Block lastBlock = null;
        int lastBlockIndex = -1;
        int currentBlockCount = 0;
//        List<BlockDef> blockDefList = new ArrayList<>();
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    Block b = playerInstance.getBlock(playerPos.blockX() + x, playerPos.blockY() + y, playerPos.blockZ() + z, BlockGetter.Condition.NONE);
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
                            resultStructure.addToBlockDefList(new Structure.BlockDef(lastBlockIndex, currentBlockCount));
                            currentBlockCount = 1;
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
        resultStructure.addToBlockDefList(new Structure.BlockDef(lastBlockIndex, currentBlockCount));

        for (int key : blockMap.keySet()) {
            resultStructure.putInBlockMap(key, blockMap.get(key));
        }

        // TODO - don't do this, actually go somewhere reasonable
        Path root = FileSystems.getDefault().getPath("..").toAbsolutePath();
        Path filePath = Paths.get(root.toString(), "src", "main", "resources", "test.json");
        StructureWriter structureWriter = new JsonStructureIO();
        structureWriter.writeStructure(resultStructure, filePath);
    }

    private void onHelp(CommandSender sender, CommandContext context) {
        version(sender, NAME, VERSION);

        sender.sendMessage("Test builder help...");
    }
}