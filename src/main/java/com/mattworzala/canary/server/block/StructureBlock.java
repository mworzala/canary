package com.mattworzala.canary.server.block;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataImpl;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.packet.client.play.ClientUpdateStructureBlockPacket;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.Objects;

import static net.minestom.server.network.packet.client.play.ClientUpdateStructureBlockPacket.*;

public class StructureBlock extends CustomBlock {
    public static final String ID = "canary:structure_block";

    public StructureBlock() {
        super(Block.STRUCTURE_BLOCK, ID);

        MinecraftServer.getPacketListenerManager().setListener(ClientUpdateStructureBlockPacket.class, (packet, player) -> {
            switch (packet.action) {
                case UPDATE_DATA:
                    // Ensure block is valid
                    Instance instance = Objects.requireNonNull(player.getInstance());
                    if (instance.getBlock(packet.location) != Block.STRUCTURE_BLOCK)
                        return;
                    Data data = Objects.requireNonNull(instance.getBlockData(packet.location));

                    data.set("name", packet.name);
                    data.set("mode", packet.mode.name());
                    data.set("showair", (packet.flags & SHOW_AIR) == SHOW_AIR);
                    data.set("offset", packet.offset);
                    data.set("size", packet.size);
                    // instance.setBlockData(packet.location, data);

                    BlockEntityDataPacket updatePacket = createBlockUpdatePacket(packet.location, data);
                    PacketUtils.sendGroupedPacket(instance.getPlayers(), updatePacket);
                    break;
                case SAVE:
                    player.sendMessage("Nothing to save!");
                    break;
                case LOAD:
                    player.sendMessage("Nothing to load!");
                    break;
                case DETECT_SIZE:
                    break;
            }
        });
    }

    @Override
    public void onPlace(@NotNull Instance instance, @NotNull BlockPosition position, @Nullable Data data) {
        Check.notNull(data, "Structure block must have associated data.");
        BlockEntityDataPacket packet = createBlockUpdatePacket(position, data);
        PacketUtils.sendGroupedPacket(instance.getPlayers(), packet);
    }

    @Override
    public void onDestroy(@NotNull Instance instance, @NotNull BlockPosition blockPosition, @Nullable Data data) {

    }

    @Override
    public boolean onInteract(@NotNull Player player, Player.@NotNull Hand hand, @NotNull BlockPosition blockPosition, @Nullable Data data) {
        return true;
    }

    @Override
    public @Nullable Data createData(@NotNull Instance instance, @NotNull BlockPosition blockPosition, @Nullable Data rawData) {
        final Data data = rawData != null ? rawData : new DataImpl();

        // Set defaults
        if (!data.hasKey("name"))
            data.set("name", "");
        if (!data.hasKey("mode"))
            data.set("mode", "SAVE");
        if (!data.hasKey("showair"))
            data.set("showair", false);
        if (!data.hasKey("offset"))
            data.set("offset", new BlockPosition(0, 1, 0));
        if (!data.hasKey("size"))
            data.set("size", new BlockPosition(2, 2, 2));

        return data;
    }

    @Override
    public void writeBlockEntity(@NotNull BlockPosition position, @Nullable Data data, @NotNull NBTCompound nbt) {
        Check.notNull(data, "Structure block must have associated data.");
        applyNbt(nbt, position, data);
    }

    @Override
    public short getCustomBlockId() {
        return Short.MAX_VALUE - 1;
    }

    private BlockEntityDataPacket createBlockUpdatePacket(@NotNull BlockPosition position, @NotNull Data data) {
        BlockEntityDataPacket packet = new BlockEntityDataPacket();
        packet.blockPosition = position;
        packet.action = 0x7;
        packet.nbtCompound = new NBTCompound();
        applyNbt(packet.nbtCompound, position, data);
        return packet;
    }

    @SuppressWarnings("ConstantConditions")
    private void applyNbt(@NotNull NBTCompound nbt, @NotNull BlockPosition position, @NotNull Data data) {
        nbt.setString("name", data.get("name"));
        nbt.setString("mode", data.get("mode"));
        nbt.setByte("showair", (byte) (data.get("showair") ? 1 : 0));
        nbt.setInt("x", position.getX());
        nbt.setInt("y", position.getY());
        nbt.setInt("z", position.getZ());
        BlockPosition offset = data.get("offset");
        nbt.setInt("posX", offset.getX());
        nbt.setInt("posY", offset.getY());
        nbt.setInt("posZ", offset.getZ());
        BlockPosition size = data.get("size");
        nbt.setInt("sizeX", size.getX());
        nbt.setInt("sizeY", size.getY());
        nbt.setInt("sizeZ", size.getZ());

        // Constant tags
        {
            nbt.setString("id", Block.STRUCTURE_BLOCK.getBlockEntityName().toString());
            nbt.setByte("keepPacked", (byte) 0);
            nbt.setString("author", "?");
            nbt.setByte("ignoreEntities", (byte) 1);
            nbt.setFloat("integrity", 1.0f);
            nbt.setString("metadata", "");
            nbt.setString("mirror", "NONE");
            nbt.setByte("powered", (byte) 0);
            nbt.setString("rotation", "NONE");
            nbt.setLong("seed", 0);
            nbt.setByte("showboundingbox", (byte) 1);
        }
    }
}
