package com.example.extension.minecart;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RailPlacementRule extends BlockPlacementRule {
    public RailPlacementRule() {
        super(Block.RAIL);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Block currentBlock) {
        return calculateBlockState(instance, currentBlock, blockPosition, null);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull Instance instance, @NotNull Block block, @NotNull BlockFace blockFace, @NotNull Point blockPosition, @NotNull Player player) {
        return calculateBlockState(instance, block, blockPosition.withY(y -> y + 1), player);
    }

    private Block calculateBlockState(@NotNull Instance instance, @NotNull Block block, @NotNull Point blockPosition, @Nullable Player player) {
        boolean north = isRailInDirection(instance, blockPosition, Direction.NORTH);
        boolean south = isRailInDirection(instance, blockPosition, Direction.SOUTH);
        boolean east = isRailInDirection(instance, blockPosition, Direction.EAST);
        boolean west = isRailInDirection(instance, blockPosition, Direction.WEST);

        int nRails = (north ? 1 : 0) + (south ? 1 : 0) + (east ? 1 : 0) + (west ? 1 : 0);
        int mask = (north ? 0x8 : 0) | (south ? 0x4 : 0) | (east ? 0x2 : 0) | (west ? 0x1 : 0);

        // Do not update from neighbors unless there are exactly 2 neighbors
        if (player == null && nRails != 2)
            return block;

        if (player != null && mask == 0) {
            Direction direction = getHorizontalFacingDirection(player);
            return Block.RAIL.withProperty("shape", direction == Direction.NORTH || direction == Direction.SOUTH ? "north_south" : "east_west");
        }

        return Block.RAIL.withProperty("shape", HORIZONTAL_SHAPES[mask]);

    }

    private static final String[] HORIZONTAL_SHAPES = new String[]{
                            // NSEW
            "",             // 0000
            "east_west",    // 0001
            "east_west",    // 0010
            "east_west",    // 0011
            "north_south",  // 0100
            "south_west",   // 0101
            "south_east",   // 0110
            "south_east",   // 0111
            "north_south",  // 1000
            "north_west",   // 1001
            "north_east",   // 1010
            "north_east",   // 1011
            "north_south",  // 1100
            "south_west",   // 1101
            "south_east",   // 1110
            "south_east",   // 1111
    };

    private boolean isRailInDirection(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Direction direction) {
        return instance.getBlock(blockPosition.add(direction.normalX(), direction.normalY(), direction.normalZ())).id() == Block.RAIL.id();
    }

    private Direction getHorizontalFacingDirection(@NotNull Player player) {
        float yaw = player.getPosition().yaw() + 180;
        // Get NSEW from yaw
        if (yaw >= 45 && yaw < 135) {
            return Direction.EAST;
        } else if (yaw >= 135 && yaw < 225) {
            return Direction.NORTH;
        } else if (yaw >= 225 && yaw < 315) {
            return Direction.WEST;
        } else {
            return Direction.SOUTH;
        }
    }
}
