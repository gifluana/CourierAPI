package com.lunazstudios.courierapi.server;

import com.google.gson.*;
import com.lunazstudios.courierapi.Courierapi;
import com.lunazstudios.courierapi.api.CourierAPI;
import com.lunazstudios.courierapi.api.Notification;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Polls {@code couriernotifications_queue.json} in the server's root directory
 * every 5 seconds (100 ticks) and dispatches any pending notifications.
 *
 * <p>After processing, the file is reset to {@code []} so entries are not sent twice.
 * If the file does not exist it is silently ignored.
 *
 * <p>Each entry in the array may reference a pre-defined notification by ID,
 * or define one inline. An optional {@code "target"} field specifies the recipient
 * player name; omit it (or set {@code "all"}) to broadcast to everyone.
 *
 * <p>Example {@code couriernotifications_queue.json}:
 * <pre>{@code
 * [
 *   { "id": "restart_warning" },
 *   { "id": "event_start", "target": "Steve" },
 *   {
 *     "title": "Inline Notice",
 *     "description": "A one-off notification without a saved ID.",
 *     "duration": 5,
 *     "target": "all"
 *   }
 * ]
 * }</pre>
 */
public final class JsonNotificationWatcher {

    private static final String FILE_NAME   = "couriernotifications_queue.json";
    private static final int    CHECK_EVERY = 100;

    private static int tickCount = 0;

    private JsonNotificationWatcher() {}

    /**
     * Called every server tick. Triggers a file check every {@value CHECK_EVERY} ticks.
     *
     * @param server the running server instance
     */
    public static void onServerTick(MinecraftServer server) {
        if (++tickCount < CHECK_EVERY) return;
        tickCount = 0;
        processFile(server);
    }

    private static void processFile(MinecraftServer server) {
        Path file = server.getRunDirectory().resolve(FILE_NAME);
        if (!Files.exists(file)) return;

        try {
            String raw = Files.readString(file).strip();
            if (raw.isEmpty() || raw.equals("[]")) return;

            JsonArray array = JsonParser.parseString(raw).getAsJsonArray();
            if (array.isEmpty()) return;

            for (JsonElement el : array) {
                try {
                    processEntry(server, el.getAsJsonObject());
                } catch (Exception e) {
                    Courierapi.LOGGER.warn("[CourierAPI] Skipping malformed queue entry: {}", e.getMessage());
                }
            }

            Files.writeString(file, "[]");

        } catch (Exception e) {
            Courierapi.LOGGER.error("[CourierAPI] Failed to process {}: {}", FILE_NAME, e.getMessage());
        }
    }

    private static void processEntry(MinecraftServer server, JsonObject obj) {
        Notification notification;

        if (obj.has("id")) {
            String id = obj.get("id").getAsString();
            Optional<Notification> defined = NotificationDefinitions.get(id);
            if (defined.isEmpty()) {
                Courierapi.LOGGER.warn("[CourierAPI] Queue references unknown id '{}'. "
                        + "Check couriernotifications.json or run /courier reload.", id);
                return;
            }
            notification = defined.get();
        } else {
            notification = NotificationJsonParser.parse(obj);
            if (notification == null) return;
        }

        String target = NotificationJsonParser.getString(obj, "target", "all");
        dispatch(server, notification, target);
    }

    private static void dispatch(MinecraftServer server, Notification notification, String target) {
        if (target.equalsIgnoreCase("all")) {
            CourierAPI.broadcast(server, notification);
            return;
        }
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(target);
        if (player != null) {
            CourierAPI.send(player, notification);
        } else {
            Courierapi.LOGGER.warn("[CourierAPI] Player '{}' not found, notification skipped.", target);
        }
    }
}
