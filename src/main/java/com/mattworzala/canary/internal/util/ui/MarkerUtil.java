package com.mattworzala.canary.internal.util.ui;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import net.minestom.server.utils.binary.BinaryWriter;

public class MarkerUtil {
    public static void sendTestMarker(Player player, Marker marker) {
        BinaryWriter buffer = new BinaryWriter();
        marker.write(buffer);
        buffer.writeInt(50000);

        PluginMessagePacket messagePacket = new PluginMessagePacket("minecraft:debug/game_test_add_marker", buffer.toByteArray());
        player.sendPacket(messagePacket);
    }

    public static record Marker(Point position, int color, String message) {
        public void write(BinaryWriter writer) {
            writer.writeBlockPosition(position);
            writer.writeInt(color);
            writer.writeSizedString(message);
        }
    }
}
