package com.mattworzala.canary.server.recording;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.client.play.ClientKeepAlivePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class PacketRecorder {
    private static final EventFilter<PlayerPacketEvent, Player> FILTER_PLAYER_PACKET = EventFilter.from(PlayerPacketEvent.class, Player.class, PlayerPacketEvent::getPlayer);
    private static final EventNode<PlayerPacketEvent> PACKET_NODE = EventNode.type("PacketRecorder_PlayerPacket", FILTER_PLAYER_PACKET);
    static { MinecraftServer.getGlobalEventHandler().addChild(PACKET_NODE); }

    private final Player target;
    private final List<PacketRecording.Record> packets = new CopyOnWriteArrayList<>();
    private final Map<Class<? extends ClientPlayPacket>, List<Predicate<ClientPlayPacket>>> filters = new HashMap<>();

    private boolean active = false;
    private Pos startPos;
    private long startTime = 0;

    public PacketRecorder(Player target) {
        this.target = target;

        addFilter(ClientChatMessagePacket.class, packet -> false);
        addFilter(ClientKeepAlivePacket.class, packet -> false);
    }

    public void start() {
        var packetListener = EventListener.builder(PlayerPacketEvent.class)
                .filter(event -> event.getPlayer().getUuid().equals(target.getUuid()))
                .handler(event -> this.onNewPacket(event.getPacket()))
                .expireWhen(event -> !active).build();
        PACKET_NODE.addListener(packetListener);

        startPos = target.getPosition();
        startTime = System.currentTimeMillis();
        active = true;
    }

    public PacketRecording stop() {
        active = false;

        var packets = new ArrayList<>(this.packets); // defensive copy
        var recording = new PacketRecording(startPos, packets);

        startTime = 0;
        this.packets.clear();
        return recording;
    }

    public <T extends ClientPlayPacket> void addFilter(Class<T> type, Predicate<T> filter) {
        var typedFilters = filters.computeIfAbsent(type, t -> new ArrayList<>());
        typedFilters.add((Predicate<ClientPlayPacket>) filter);
    }

    private void onNewPacket(@NotNull ClientPacket packet) {
        if (!(packet instanceof ClientPlayPacket playPacket))
            return;

        // Filtering
        var typedFilters = filters.get(playPacket.getClass());
        if (typedFilters != null) {
            for (var filter : typedFilters) {
                if (!filter.test(playPacket))
                    return;
            }
        }

        // Append entry
        int tickDelta = (int) (System.currentTimeMillis() - startTime);
        packets.add(new PacketRecording.Record(tickDelta, playPacket));
    }
}
