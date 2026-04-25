package com.lunazstudios.courierapi.server;

import com.google.gson.JsonObject;
import com.lunazstudios.courierapi.Courierapi;
import com.lunazstudios.courierapi.api.Notification;

/**
 * Utility class for parsing {@link Notification} objects from JSON.
 *
 * <p>Accepts both 6-digit ({@code #RRGGBB}) and 8-digit ({@code #AARRGGBB}) hex color strings.
 * 6-digit values are treated as fully opaque (alpha = 0xFF).
 */
public final class NotificationJsonParser {

    private NotificationJsonParser() {}

    /**
     * Parses a {@link Notification} from a JSON object.
     * Returns {@code null} and logs a warning if the required {@code "title"} field is missing.
     *
     * @param obj the JSON object to parse
     * @return the parsed notification, or {@code null} on failure
     */
    public static Notification parse(JsonObject obj) {
        if (!obj.has("title")) {
            Courierapi.LOGGER.warn("[CourierAPI] Notification entry missing 'title', skipping.");
            return null;
        }

        String title         = obj.get("title").getAsString();
        String description   = getString(obj, "description", "");
        float  duration      = getFloat(obj, "duration", 5f);

        int titleColor       = parseColor(obj, "title_color",       Notification.DEFAULT_TITLE_COLOR);
        int descriptionColor = parseColor(obj, "description_color", Notification.DEFAULT_DESCRIPTION_COLOR);
        int backgroundColor  = parseColor(obj, "background_color",  Notification.DEFAULT_BACKGROUND_COLOR);
        int borderColor      = parseColor(obj, "border_color",      Notification.DEFAULT_BORDER_COLOR);

        return Notification.builder(title, description)
                .durationSeconds(duration)
                .titleColor(titleColor)
                .descriptionColor(descriptionColor)
                .backgroundColor(backgroundColor)
                .borderColor(borderColor)
                .build();
    }

    /**
     * Returns the string value of {@code key}, or {@code def} if absent.
     */
    public static String getString(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }

    /**
     * Returns the float value of {@code key}, or {@code def} if absent.
     */
    public static float getFloat(JsonObject obj, String key, float def) {
        return obj.has(key) ? obj.get(key).getAsFloat() : def;
    }

    /**
     * Parses an ARGB color from a hex string field.
     * Returns {@code def} if the field is absent or the value is malformed.
     *
     * @param obj the source JSON object
     * @param key the field name
     * @param def the fallback ARGB color
     */
    public static int parseColor(JsonObject obj, String key, int def) {
        if (!obj.has(key)) return def;
        String raw = obj.get(key).getAsString().replace("#", "").strip();
        try {
            long v = Long.parseLong(raw, 16);
            if (raw.length() <= 6) v |= 0xFF000000L;
            return (int) v;
        } catch (NumberFormatException e) {
            Courierapi.LOGGER.warn("[CourierAPI] Invalid color '{}' for '{}', using default.", raw, key);
            return def;
        }
    }
}
