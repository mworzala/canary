package com.mattworzala.canary.server.recording;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public record PacketRecording(
        @NotNull Pos startPosition,
        @NotNull List<Record> packets)
        implements Iterable<PacketRecording.Record> {

    public static record Record(int timeDelta, @NotNull ClientPlayPacket packet) {}

    public int size() {
        return packets.size();
    }

    @Override
    public @NotNull Iterator<Record> iterator() {
        return packets().iterator();
    }
}
