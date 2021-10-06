package com.mattworzala.canary.server.givemeahome;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class PlayerPacketRecorder {
    private static final EventFilter<PlayerPacketEvent, Player> FILTER_PLAYER_PACKET = EventFilter.from(PlayerPacketEvent.class, Player.class, PlayerPacketEvent::getPlayer);
    private static final EventNode<PlayerPacketEvent> PACKET_NODE = EventNode.type("PacketRecorder_PlayerPacket", FILTER_PLAYER_PACKET);
    static { MinecraftServer.getGlobalEventHandler().addChild(PACKET_NODE); }

    private static final Map<Class<? extends ClientPacket>, Integer> packetToIdMap = new HashMap<>();

    private static record ClientPacketInstance(@NotNull ClientPacket packet, long delta) {}




    private final Player target;
    private final List<ClientPacketInstance> packets = new ArrayList<>();

    private boolean active = true;
    private long startTime = System.currentTimeMillis();

    public PlayerPacketRecorder(@NotNull Player target) {
        this.target = target;

        var packetListener = EventListener.builder(PlayerPacketEvent.class)
                .filter(event -> event.getPlayer().getUuid().equals(target.getUuid()))
                .handler(event -> this.onNewPacket(event.getPacket()))
                .expireWhen(event -> !active).build();
        PACKET_NODE.addListener(packetListener);
    }

    public void stop() {
        active = false;



        var cph = MinecraftServer.getPacketProcessor().getPlayPacketsHandler();
        for (int i = 0; i < 100; i++) {
            try {
                ClientPacket packet = cph.getPacketInstance(i);
                packetToIdMap.put(packet.getClass(), i);
                System.out.println("parsed " + Integer.toHexString(i));
            } catch (IllegalStateException ex) {
                if (ex.getMessage().contains("debug needed")) {
                    System.out.println("stopped on  " + Integer.toHexString(i));
                    break;
                }
                System.out.println("problem: " + ex.getMessage());
            } catch (IndexOutOfBoundsException ignored) {
                System.out.println("Idx oob");
                break;
            }

        }


        BinaryWriter buffer = new BinaryWriter(1 * 1024 * 1024); //1MB max

        /* Header (32 bytes)
           - magic number: 2 bytes
           - version: 2 bytes
           - packet count: 2 bytes
           - empty: 26 bytes
         */
        buffer.writeShort((short) 0xF0F0);
        buffer.writeShort((short) 1);
        buffer.writeShort((short) packets.size());
        buffer.writeBytes(new byte[26]);

        // Packets
        for (var pi : packets) {
            int packetId = packetToIdMap.get(pi.packet().getClass());

            BinaryWriter temp = new BinaryWriter(32 * 1024); // 32KB max
            pi.packet().write(temp);

            /* Packet (x bytes, `packet count` times):
               - total len of packet + id + delta (4 bytes)
               - packet id (2 bytes)
               - time delta (2 bytes)
               - packet (x bytes)
             */
            int totalLen = 2 + 2 + temp.getBuffer().position();
            buffer.writeInt(totalLen);
            buffer.writeShort((short) packetId);
            buffer.writeShort((short) pi.delta());
            buffer.write(temp);
        }

        // Write to file
        try {
            byte[] payload = buffer.toByteArray();
            Files.write(Paths.get("./out.cpr"), payload, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }








//        new Thread(() -> {
//            try {
//                Player fakePlayer = new Player(UUID.randomUUID(), target.getUsername() + "_fake", new EmptyConnection());
//                MinecraftServer.getConnectionManager().startPlayState(fakePlayer, true);
//
//                Thread.sleep(1000); // Give enough time for player to spawn
//
//                fakePlayer.refreshReceivedTeleportId(fakePlayer.getLastSentTeleportId());
//
//                long lastTime = 0;
//                for (var packet : packets) {
//                    Thread.sleep(packet.delta - lastTime);
//                    lastTime += packet.delta - lastTime;
//
//                    fakePlayer.addPacketToQueue((ClientPlayPacket) packet.packet());
//                    System.out.println("Sent" + packet.packet().getClass().getName());
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    private void onNewPacket(@NotNull ClientPacket packet) {
        System.out.println("RECEIVED PACKET: " + packet.getClass().getName());
        if (packet instanceof ClientChatMessagePacket) return;
        packets.add(new ClientPacketInstance(packet, System.currentTimeMillis() - startTime));
    }

    private static class EmptyConnection extends PlayerConnection {

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
