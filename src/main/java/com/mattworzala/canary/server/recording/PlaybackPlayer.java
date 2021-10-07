package com.mattworzala.canary.server.recording;

import com.mattworzala.canary.platform.util.StringUtil;
import com.mattworzala.canary.server.givemeahome.DesyncedTicker;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class PlaybackPlayer extends Player {
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    /** Schedules ticks for all running playbacks */
    private static final DesyncedTicker TICKER = new DesyncedTicker(50, TimeUnit.MILLISECONDS);

    private final PacketRecording recording;

    // Running state
    private List<PacketRecording.Record> packets = null;
    private long startTime;

    public PlaybackPlayer(@NotNull PacketRecording recording) {
        this(UUID.randomUUID(), recording);
    }

    public PlaybackPlayer(@NotNull UUID uuid, @NotNull PacketRecording recording) {
        super(uuid, "playback_" + StringUtil.randomString(5), new EmptyPlayerConnection());
        this.recording = recording;

        CONNECTION_MANAGER.startPlayState(this, true);
    }

    public void start() {
        refreshReceivedTeleportId(getLastSentTeleportId());

        packets = new ArrayList<>(recording.packets());
        startTime = System.currentTimeMillis();

        TICKER.add(this);
    }

    public void stop() {
        TICKER.remove(this);
    }



    @Override
    public void tick(long time) {
        if (time != -1) {
            super.tick(time);
            return;
        }

        long delta = System.currentTimeMillis() - startTime;

        var iter = packets.iterator();
        while (iter.hasNext()) {
            PacketRecording.Record rec = iter.next();

            if (rec.timeDelta() <= delta) {
                addPacketToQueue(rec.packet());
                iter.remove();
            }
        }

        if (packets.isEmpty()) {
            stop();
        }
    }

    private static class EmptyPlayerConnection extends PlayerConnection {
        //todo is this useful for test reporting? (eg we received this server packet, we were disconnected, etc)

        @Override
        public void sendPacket(@NotNull ServerPacket serverPacket, boolean skipTranslating) {

        }

        @Override
        public @NotNull SocketAddress getRemoteAddress() {
            return new InetSocketAddress(0);
        }

        @Override
        public void disconnect() {

        }
    }
}
