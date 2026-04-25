package com.lunazstudios.courierapi.client;

import com.lunazstudios.courierapi.api.Notification;

/**
 * Tracks the animation state of a {@link Notification} currently on screen.
 *
 * <p>Each notification transitions through four phases:
 * <ol>
 *   <li>{@link Phase#SLIDE_IN}  — slides in from the right over {@value SLIDE_MS} ms.</li>
 *   <li>{@link Phase#VISIBLE}   — fully visible for the notification's duration.</li>
 *   <li>{@link Phase#SLIDE_OUT} — slides back out to the right over {@value SLIDE_MS} ms.</li>
 *   <li>{@link Phase#DONE}      — animation complete; the entry is removed from the queue.</li>
 * </ol>
 */
public class ActiveNotification {

    private static final long SLIDE_MS = 250;

    /** The current animation phase of the notification. */
    public enum Phase { SLIDE_IN, VISIBLE, SLIDE_OUT, DONE }

    private final Notification notification;
    private final long startMs;
    private final long visibleMs;
    private Phase phase = Phase.SLIDE_IN;

    public ActiveNotification(Notification notification) {
        this.notification = notification;
        this.startMs      = System.currentTimeMillis();
        this.visibleMs    = (notification.durationTicks() * 1000L) / 20L;
    }

    /** Returns the underlying notification data. */
    public Notification notification() { return notification; }

    /** Returns the current animation phase. */
    public Phase phase() { return phase; }

    /** Returns {@code true} when the animation is fully complete and this entry can be discarded. */
    public boolean isDone() { return phase == Phase.DONE; }

    /** Advances the animation phase based on elapsed time. Called every client tick. */
    public void update() {
        long elapsed = elapsed();
        if      (elapsed < SLIDE_MS)                  phase = Phase.SLIDE_IN;
        else if (elapsed < SLIDE_MS + visibleMs)      phase = Phase.VISIBLE;
        else if (elapsed < SLIDE_MS * 2 + visibleMs)  phase = Phase.SLIDE_OUT;
        else                                           phase = Phase.DONE;
    }

    /**
     * Returns a {@code [0, 1]} progress value for the slide animation.
     * {@code 1.0} means fully on screen; {@code 0.0} means fully off screen.
     */
    public float slideProgress() {
        long e = elapsed();
        return switch (phase) {
            case SLIDE_IN  -> clamp01((float) e / SLIDE_MS);
            case VISIBLE   -> 1f;
            case SLIDE_OUT -> clamp01(1f - (float)(e - SLIDE_MS - visibleMs) / SLIDE_MS);
            case DONE      -> 0f;
        };
    }

    /**
     * Returns a {@code [0, 1]} progress value representing remaining visible time.
     * Used to drive the progress bar ({@code 1.0} = full, {@code 0.0} = expired).
     */
    public float visibleProgress() {
        long e = elapsed();
        if (e < SLIDE_MS)             return 1f;
        if (e > SLIDE_MS + visibleMs) return 0f;
        return clamp01(1f - (float)(e - SLIDE_MS) / visibleMs);
    }

    private long elapsed() { return System.currentTimeMillis() - startMs; }

    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
}
