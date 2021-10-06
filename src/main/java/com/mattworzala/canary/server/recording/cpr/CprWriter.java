package com.mattworzala.canary.server.recording.cpr;

import com.mattworzala.canary.server.recording.PacketRecording;
import com.mattworzala.canary.server.recording.PacketRecordingWriter;
import it.unimi.dsi.fastutil.objects.Object2ShortArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.handler.ClientPacketsHandler;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CprWriter implements PacketRecordingWriter {
    public static final short MAGIC_NUMBER = 0x41FF;
    public static final short VERSION = 1;

    private static final int BUFFER_SIZE = 1 * 1024 * 1024; // 1MB

    private final OutputStream output;
    private final BinaryWriter buffer;

    public CprWriter(@NotNull Path path) throws IOException {
        this(Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE));
    }

    public CprWriter(@NotNull OutputStream output) {
        this.output = output;
        this.buffer = new BinaryWriter(BUFFER_SIZE);

        ClientPacketIdMapper.init();
    }

    @Override
    public void write(@NotNull PacketRecording recording) throws IOException {
        // Reset existing buffer
        buffer.getBuffer().clear();

        // Write to buffer
        writeHeader(recording);

        for (var packet : recording) {
            writePacket(packet);
        }

        // Write to output stream
        output.write(buffer.toByteArray());
    }

    @Override
    public void close() throws Exception {
        output.close();
        buffer.close();
    }

    /**
     * Writes a .cpr file header with the following format (64 bytes long in total):
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
     * @param recording The recording for which to write a header
     */
    private void writeHeader(@NotNull PacketRecording recording) {
        buffer.writeShort(MAGIC_NUMBER);
        buffer.writeShort(VERSION);
        buffer.writeShort((short) recording.size());

        var startPos = recording.startPosition();
        buffer.writeDouble(startPos.x());
        buffer.writeDouble(startPos.y());
        buffer.writeDouble(startPos.z());
        buffer.writeDouble(startPos.pitch());
        buffer.writeDouble(startPos.yaw());

        buffer.writeBytes(new byte[26]);
    }

    /**
     * Writes a single packet record with the following format:
     * <p>2 bytes | tick delta (# of ticks after recording start)</p>
     * <p>2 bytes | packet id</p>
     * <p>N bytes | raw packet data</p>
     *
     * @param rec The packet record to write
     */
    private void writePacket(PacketRecording.Record rec) {
        // Get packet id
        short packetId = ClientPacketIdMapper.getPacketId(rec.packet());

        // Write entry
        buffer.writeShort(rec.tickDelta());
        buffer.writeShort(packetId);
        rec.packet().write(buffer);
    }

    private static class ClientPacketIdMapper {
        private static final Object2ShortMap<Class<? extends ClientPacket>> packetToId = new Object2ShortArrayMap<>();
        private static volatile boolean initialized = false;

        public static void init() {
            if (initialized) return;
            initialized = true;

            ClientPacketsHandler packetsHandler = MinecraftServer.getPacketProcessor().getPlayPacketsHandler();
            for (short i = 0; ; i++) {
                try {
                    ClientPacket packet = packetsHandler.getPacketInstance(i);
                    packetToId.put(packet.getClass(), i);
                } catch (IllegalStateException e) {
                    if (e.getMessage().contains("debug needed")) {
                        break;
                    }
                } catch (IndexOutOfBoundsException ignored) {
                    break;
                }
            }
        }

        public static short getPacketId(@NotNull ClientPacket packet) throws IllegalArgumentException {
            Check.argCondition(packetToId.containsKey(packet.getClass()), "Unknown packet: " + packet.getClass().getName());
            return packetToId.getShort(packet.getClass());
        }
    }
}
