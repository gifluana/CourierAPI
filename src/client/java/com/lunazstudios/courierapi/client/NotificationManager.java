package com.lunazstudios.courierapi.client;

import com.lunazstudios.courierapi.api.Notification;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Manages the client-side notification queue and the list of actively animating notifications.
 *
 * <p>At most {@value MAX_VISIBLE} notifications are shown simultaneously.
 * Additional notifications are held in a pending queue and promoted as slots open up.
 */
public final class NotificationManager {

    private static final int MAX_VISIBLE = 4;

    private static final List<ActiveNotification> active  = new ArrayList<>();
    private static final Deque<Notification>      pending = new ArrayDeque<>();

    private NotificationManager() {}

    /**
     * Adds a notification to the pending queue.
     * It will appear on screen as soon as a display slot is available.
     *
     * @param notification the notification to enqueue
     */
    public static void queue(Notification notification) {
        pending.addLast(notification);
    }

    /** Returns the list of notifications currently being animated on screen. */
    public static List<ActiveNotification> getActive() {
        return active;
    }

    /**
     * Advances all active animations and promotes pending notifications into empty slots.
     * Called every client tick.
     */
    public static void tick() {
        active.forEach(ActiveNotification::update);
        active.removeIf(ActiveNotification::isDone);

        while (active.size() < MAX_VISIBLE && !pending.isEmpty()) {
            active.add(new ActiveNotification(pending.pollFirst()));
        }
    }
}
