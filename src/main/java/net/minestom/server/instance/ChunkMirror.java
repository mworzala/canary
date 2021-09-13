package net.minestom.server.instance;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.CachedPacket;
import net.minestom.server.network.packet.FramedPacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChunkMirror extends Chunk {
    private final DynamicChunk mirror;

    // start:DynamicChunk
    private final CachedPacket chunkCache = new CachedPacket(this::createChunkPacket);
    private final CachedPacket lightCache = new CachedPacket(this::createLightPacket);
    // end:DynamicChunk

    public ChunkMirror(@NotNull Instance instance, @NotNull DynamicChunk mirror, int targetX, int targetZ) {
        super(instance, mirror.getBiomes(), targetX, targetZ, mirror.shouldGenerate());

        this.mirror = mirror;
    }

    @Override
    public @NotNull Map<Integer, Section> getSections() {
        return mirror.getSections();
    }

    @Override
    public @NotNull Section getSection(int section) {
        return mirror.getSection(section);
    }

    @Override
    public void tick(long time) {
        mirror.tick(time);
    }

    @Override
    public long getLastChangeTime() {
        return mirror.getLastChangeTime();
    }

    @Override
    public void sendChunk(@NotNull Player player) {
        // start:DynamicChunk
        if (!isLoaded()) return;
        final PlayerConnection connection = player.getPlayerConnection();
        final long lastChange = getLastChangeTime();
        final FramedPacket lightPacket = lightCache.retrieveFramedPacket(lastChange);
        final FramedPacket chunkPacket = chunkCache.retrieveFramedPacket(lastChange);
        if (connection instanceof PlayerSocketConnection) {
            PlayerSocketConnection socketConnection = (PlayerSocketConnection) connection;
            socketConnection.write(lightPacket.body());
            socketConnection.write(chunkPacket.body());
        } else {
            connection.sendPacket(lightPacket.packet());
            connection.sendPacket(chunkPacket.packet());
        }
        // end:DynamicCHunk
    }

    @Override
    public void sendChunk() {
        // start:DynamicChunk
        if (!isLoaded()) return;
        if (getViewers().isEmpty()) return;
        final long lastChange = getLastChangeTime();
        final FramedPacket lightPacket = lightCache.retrieveFramedPacket(lastChange);
        final FramedPacket chunkPacket = chunkCache.retrieveFramedPacket(lastChange);
        sendPacketToViewers(lightPacket.packet());
        sendPacketToViewers(chunkPacket.packet());
        // end:DynamicChunk
    }

    @Override
    public @NotNull Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
        return mirror.copy(instance, chunkX, chunkZ);
    }

    @Override
    public void reset() {
        mirror.reset();
    }

    @Override
    public @Nullable Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        return mirror.getBlock(x, y, z);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        mirror.setBlock(x, y, z, block);
    }

    // start:DynamicChunk
    private @NotNull ChunkDataPacket createChunkPacket() {
        ChunkDataPacket packet = new ChunkDataPacket();
        packet.biomes = mirror.biomes;
        packet.chunkX = chunkX;
        packet.chunkZ = chunkZ;
        packet.sections = mirror.sectionMap.clone(); // TODO deep clone
        packet.entries = mirror.entries.clone();
        return packet;
    }

    private @NotNull UpdateLightPacket createLightPacket() {
        long skyMask = 0;
        long blockMask = 0;
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        UpdateLightPacket updateLightPacket = new UpdateLightPacket();
        updateLightPacket.chunkX = getChunkX();
        updateLightPacket.chunkZ = getChunkZ();

        updateLightPacket.skyLight = skyLights;
        updateLightPacket.blockLight = blockLights;

        final var sections = getSections();
        for (var entry : sections.entrySet()) {
            final int index = entry.getKey() + 1;
            final Section section = entry.getValue();

            final var skyLight = section.getSkyLight();
            final var blockLight = section.getBlockLight();

            if (!ArrayUtils.empty(skyLight)) {
                skyLights.add(skyLight);
                skyMask |= 1L << index;
            }
            if (!ArrayUtils.empty(blockLight)) {
                blockLights.add(blockLight);
                blockMask |= 1L << index;
            }
        }

        updateLightPacket.skyLightMask = new long[]{skyMask};
        updateLightPacket.blockLightMask = new long[]{blockMask};
        updateLightPacket.emptySkyLightMask = new long[0];
        updateLightPacket.emptyBlockLightMask = new long[0];
        return updateLightPacket;
    }
    // end:DynamicCHunk
}
