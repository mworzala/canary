package com.mattworzala.canary.internal.util.ui;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CameraPlayer extends Player {
    public static volatile boolean DO_FORWARDING = true;
    public static volatile boolean DO_DEBUG_LOG = false;

    private final List<Player> viewers;

    public CameraPlayer(@NotNull Instance instance, @NotNull Pos position, @NotNull List<Player> viewers) {
        super(instance.getUniqueId(), generateName(), new Connection(viewers));
        this.viewers = viewers;

        MinecraftServer.getGlobalEventHandler().addListener(
                EventListener.builder(PlayerSpawnEvent.class)
                        .expireCount(1)
                        .filter(event -> event.getPlayer().equals(this))
                        .handler(event -> {
                            try {
                                this.setInstance(instance, position);
                            } catch (IllegalArgumentException ignored) {
                                this.teleport(position);
                            }
                        })
                        .build());

        MinecraftServer.getConnectionManager().startPlayState(this, false);
    }

    public void addCameraViewer(@NotNull Player player) {
        viewers.add(player);
    }

    public void removeCameraViewer(@NotNull Player player) {
        viewers.remove(player);
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        handleTabList(player.getPlayerConnection());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void showPlayer(@NotNull PlayerConnection connection) {
        super.showPlayer(connection);
        handleTabList(connection);
    }

    private void handleTabList(PlayerConnection connection) {
        // Remove from tab-list
        MinecraftServer.getSchedulerManager().buildTask(() -> connection.sendPacket(getRemovePlayerToList())).delay(20, TimeUnit.SERVER_TICK).schedule();
    }

    @NotNull
    private static String generateName() {
        return "_cny_" + ThreadLocalRandom.current().ints(97, 123)
                .limit(10)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append);
    }

    private static class Connection extends PlayerConnection {

        private static final List<Class<?>> debugBlacklist = List.of(
                EntityPositionAndRotationPacket.class,
                EntityPositionPacket.class,
                EntityRotationPacket.class,
                TimeUpdatePacket.class
        );

        public static List<Class<?>> forwardWhitelist = List.of(
                SpawnLivingEntityPacket.class,
                EntityMetaDataPacket.class,
                EntityEquipmentPacket.class,
                EntityPropertiesPacket.class,
                EntityTeleportPacket.class,
                EntityPositionAndRotationPacket.class,
                EntityPositionPacket.class,
                EntityRotationPacket.class,
                EntityHeadLookPacket.class
        );

        private final List<Player> viewers;

        private Connection(@NotNull List<Player> viewers) {
            this.viewers = viewers;
        }

        @Override
        public void sendPacket(@NotNull ServerPacket serverPacket, boolean skipTranslating) {

            if (forwardWhitelist.stream().anyMatch(cl -> cl.isAssignableFrom(serverPacket.getClass()))) {
                if (DO_FORWARDING)
                    viewers.forEach(target -> target.getPlayerConnection().sendPacket(serverPacket));
                if (DO_DEBUG_LOG)
                    System.out.println("FWD >> " + serverPacket.getClass().getSimpleName());
            } else if (debugBlacklist.stream().noneMatch(cl -> cl.isAssignableFrom(serverPacket.getClass()))) {
                if (DO_DEBUG_LOG)
                    System.out.println("NOFWD >> " + serverPacket.getClass().getSimpleName());
            }
        }

        @NotNull
        @Override
        public SocketAddress getRemoteAddress() {
            return new InetSocketAddress(0);
        }

        @Override
        public void disconnect() {

        }

        @Override
        public void setPlayer(Player player) {
            Check.argCondition(!(player instanceof CameraPlayer), "CameraPlayer.Controller needs a CameraPlayer object");
            super.setPlayer(player);
        }
    }
}
