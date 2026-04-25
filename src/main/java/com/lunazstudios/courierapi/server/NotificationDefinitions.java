package com.lunazstudios.courierapi.server;

import com.google.gson.*;
import com.lunazstudios.courierapi.Courierapi;
import com.lunazstudios.courierapi.api.Notification;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Loads and caches named notification definitions from {@code couriernotifications.json}
 * in the server's root directory.
 *
 * <p>Definitions are referenced by ID in commands and the queue file.
 * The file is created automatically with a sample entry on first run.
 *
 * <p>Example {@code couriernotifications.json}:
 * <pre>{@code
 * {
 *   "restart_warning": {
 *     "title": "Server Restart",
 *     "description": "The server restarts in 5 minutes.",
 *     "duration": 8,
 *     "title_color": "#FFFF55",
 *     "description_color": "#FFFFFF",
 *     "background_color": "#CC000000",
 *     "border_color": "#FFFF55"
 *   }
 * }
 * }</pre>
 *
 * <p>All fields except {@code title} are optional and fall back to their defaults.
 * Colors accept both 6-digit ({@code #RRGGBB}) and 8-digit ({@code #AARRGGBB}) hex strings.
 */
public final class NotificationDefinitions {

    private static final String FILE_NAME = "couriernotifications.json";
    private static final Gson   GSON      = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, Notification> definitions = new LinkedHashMap<>();

    private NotificationDefinitions() {}

    /**
     * Clears the cache and reloads all definitions from disk.
     * Called automatically on server start and via {@code /courier reload}.
     *
     * @param server the running server instance (used to resolve the file path)
     */
    public static void reload(MinecraftServer server) {
        definitions.clear();
        Path file = server.getServerDirectory().resolve(FILE_NAME);

        if (!Files.exists(file)) {
            createDefaultFile(file);
            return;
        }

        try {
            String raw = Files.readString(file).strip();
            if (raw.isEmpty()) return;

            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                try {
                    Notification n = NotificationJsonParser.parse(entry.getValue().getAsJsonObject());
                    if (n != null) definitions.put(entry.getKey(), n);
                } catch (Exception e) {
                    Courierapi.LOGGER.warn("[CourierAPI] Skipping definition '{}': {}", entry.getKey(), e.getMessage());
                }
            }
            Courierapi.LOGGER.info("[CourierAPI] Loaded {} definition(s) from {}.", definitions.size(), FILE_NAME);

        } catch (Exception e) {
            Courierapi.LOGGER.error("[CourierAPI] Failed to load {}: {}", FILE_NAME, e.getMessage());
        }
    }

    /**
     * Returns the notification registered under the given ID, or empty if not found.
     *
     * @param id the definition ID as declared in {@code couriernotifications.json}
     */
    public static Optional<Notification> get(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    /**
     * Returns an unmodifiable view of all currently loaded definition IDs.
     * Used to power tab-completion in {@code /courier send}.
     */
    public static Set<String> getIds() {
        return Collections.unmodifiableSet(definitions.keySet());
    }

    private static void createDefaultFile(Path file) {
        JsonObject root    = new JsonObject();
        JsonObject example = new JsonObject();
        example.addProperty("title",             "Example Notification");
        example.addProperty("description",       "This is a sample notification from CourierAPI.");
        example.addProperty("duration",          5.0);
        example.addProperty("title_color",       "#FFFF55");
        example.addProperty("description_color", "#FFFFFF");
        example.addProperty("background_color",  "#CC000000");
        example.addProperty("border_color",      "#FFFF55");
        root.add("example", example);

        try {
            Files.writeString(file, GSON.toJson(root));
            Courierapi.LOGGER.info("[CourierAPI] Created default {} in server root.", FILE_NAME);
        } catch (Exception e) {
            Courierapi.LOGGER.error("[CourierAPI] Could not create default {}: {}", FILE_NAME, e.getMessage());
        }
    }
}
