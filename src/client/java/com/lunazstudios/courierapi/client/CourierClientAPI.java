package com.lunazstudios.courierapi.client;

import com.lunazstudios.courierapi.api.Notification;

/**
 * Client-side API entry point for CourierAPI.
 *
 * <p><strong>This class must only be called from client-side code.</strong>
 * Calling it on a dedicated server will crash.
 *
 * <p>Use this when you want to display a notification locally without
 * any server involvement — for example, from another client mod:
 * <pre>{@code
 * Notification n = Notification.builder("Waypoint Reached", "You arrived at your destination.")
 *         .durationSeconds(4)
 *         .build();
 *
 * CourierClientAPI.showLocal(n);
 * }</pre>
 */
public final class CourierClientAPI {

    private CourierClientAPI() {}

    /**
     * Queues a notification to be displayed on the local client's HUD immediately.
     *
     * <p>The notification is added to the render queue and shown as soon as
     * a slot is available (up to 4 notifications are visible at once).
     *
     * @param notification the notification to display
     */
    public static void showLocal(Notification notification) {
        NotificationManager.queue(notification);
    }
}
