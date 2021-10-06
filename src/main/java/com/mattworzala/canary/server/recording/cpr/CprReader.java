package com.mattworzala.canary.server.recording.cpr;

import com.mattworzala.canary.server.recording.PacketRecording;
import com.mattworzala.canary.server.recording.PacketRecordingReader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.handler.ClientPacketsHandler;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CprReader implements PacketRecordingReader {
    public static final short MAGIC_NUMBER = CprWriter.MAGIC_NUMBER;
    public static final short VERSION = CprWriter.VERSION;

    private static final PacketProcessor PACKET_PROCESSOR = MinecraftServer.getPacketProcessor();

    private static record CprHeader(short version, short packetCount, Pos startPosition) {}

    private final InputStream input;
    @TestOnly
    protected BinaryReader buffer;

    public CprReader(@NotNull Path path) throws IOException {
        this(Files.newInputStream(path, StandardOpenOption.READ));
    }

    public CprReader(@NotNull InputStream input) {
        this.input = input;
    }

    @Override
    public @NotNull PacketRecording read() throws IOException {
        buffer = new BinaryReader(input.readAllBytes());

        // Read from buffer
        CprHeader header = readHeader();
        if (header == null) {
            throw new IllegalStateException("Cannot read corrupt data from CPR file");
        }
        if (header.version() != VERSION) {
            throw new IllegalStateException("Cannot read CPR version " + header.version() + ", only " + VERSION + " is supported.");
        }

        List<PacketRecording.Record> packets = new ArrayList<>(header.packetCount);
        for (int i = 0; i < header.packetCount(); i++) {
            packets.add(readPacket());
        }

        buffer.close();
        buffer = null;
        return new PacketRecording(header.startPosition(), packets);
    }

    @Override
    public void close() throws Exception {
        input.close();
    }

    /**
     * Reads a .cpr file header with the following format (64 bytes long in total):
     * <p>2 bytes | magic number</p>
     * <p>2 bytes | version</p>
     * <p>2 bytes | # of packets in recording</p>
     * <p>8 bytes | start position x</p>
     * <p>8 bytes | start position y</p>
     * <p>8 bytes | start position z</p>
     * <p>4 bytes | start position pitch</p>
     * <p>4 bytes | start position yaw</p>
     * <p>26 bytes | unused</p>
     *
     * @return The header data if valid, null otherwise
     */
    @Nullable @TestOnly
    protected CprHeader readHeader() {
        // Confirm magic number
        if (buffer.readShort() != MAGIC_NUMBER) {
            return null;
        }

        // Read other data
        short version = buffer.readShort();
        short packetCount = buffer.readShort();
        Pos startPosition = new Pos(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readFloat(),
                buffer.readFloat());

        // Read unused bytes
        buffer.readBytes(26);

        return new CprHeader(version, packetCount, startPosition);
    }

    /**
     * Reads a single packet record with the following format:
     * <p>2 bytes | tick delta (# of ticks after recording start)</p>
     * <p>2 bytes | packet id</p>
     * <p>N bytes | raw packet data</p>
     *
     * @return A packet record
     */
    @NotNull @TestOnly
    protected PacketRecording.Record readPacket() {
        short tickDelta = buffer.readShort();
        short packetId = buffer.readShort();

        // Construct packet
        ClientPlayPacket packet = (ClientPlayPacket) PACKET_PROCESSOR.getPlayPacketsHandler().getPacketInstance(packetId);
        packet.read(buffer);

        return new PacketRecording.Record(tickDelta, packet);
    }
}
