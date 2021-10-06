package com.mattworzala.canary.server.recording;

import com.mattworzala.canary.platform.util.StringUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

public class PlaybackPlayer extends Player {
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    private final PacketRecording recording;

    private boolean running = false;
    private int tick = 0;

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
        running = true;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void update(long time) {
        if (!running) {
            super.update(time);
            return;
        }

        for (var rec : recording.packets()) {
            if (rec.tickDelta() == tick) {
                addPacketToQueue(rec.packet());
                System.out.println("Sending " + rec.packet().getClass().getSimpleName());
            }
        }

        super.update(time);

        tick++;
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
