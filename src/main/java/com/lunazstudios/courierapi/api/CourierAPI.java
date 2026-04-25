package com.lunazstudios.courierapi.api;

import com.lunazstudios.courierapi.network.NotificationPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side API entry point for CourierAPI.
 *
 * <p>Other mods should depend only on this package ({@code com.lunazstudios.courierapi.api}).
 *
 * <p>Usage example:
 * <pre>{@code
 * Notification n = Notification.builder("Event Started!", "The PvP event has begun.")
 *         .durationSeconds(6)
 *         .borderColor(0xFFFF4444)
 *         .build();
 *
 * // Send to a single player
 * CourierAPI.send(player, n);
 *
 * // Broadcast to all online players
 * CourierAPI.broadcast(server, n);
 * }</pre>
 *
 * <p>To display a notification directly on the local client without a server,
 * see {@link com.lunazstudios.courierapi.client.CourierClientAPI#showLocal(Notification)}.
 */
public final class CourierAPI {

    private CourierAPI() {}

    /**
     * Sends a notification to a single player.
     *
     * @param player       the target player
     * @param notification the notification to display
     */
    public static void send(ServerPlayer player, Notification notification) {
        ServerPlayNetworking.send(player, new NotificationPacket(notification));
    }

    /**
     * Broadcasts a notification to all currently online players.
     *
     * @param server       the running server instance
     * @param notification the notification to display
     */
    public static void broadcast(MinecraftServer server, Notification notification) {
        NotificationPacket packet = new NotificationPacket(notification);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, packet);
        }
    }
}