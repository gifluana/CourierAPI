package com.lunazstudios.courierapi.client;

import com.lunazstudios.courierapi.api.Notification;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/**
 * Renders active notifications as stacked cards in the top-right corner of the HUD.
 *
 * <p>Each card contains:
 * <ul>
 *   <li>A colored left accent border.</li>
 *   <li>The notification title and a countdown timer (top row).</li>
 *   <li>The description text (second row).</li>
 *   <li>A progress bar at the bottom that drains as time runs out.</li>
 * </ul>
 *
 * <p>Cards slide in and out from the right edge using an ease-out cubic curve.
 * Rendering is skipped entirely when the HUD is hidden ({@code F1} mode).
 */
public final class NotificationRenderer {

    private static final int W        = 210;
    private static final int H        = 50;
    private static final int MARGIN   = 6;
    private static final int GAP      = 4;
    private static final int BORDER_W = 3;
    private static final int PAD_H    = 8;
    private static final int PAD_V    = 8;
    private static final int BAR_H    = 3;

    private NotificationRenderer() {}

    /** Renders all active notifications. Called every HUD render frame. */
    public static void render(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;

        TextRenderer font = mc.textRenderer;
        List<ActiveNotification> notifications = NotificationManager.getActive();
        if (notifications.isEmpty()) return;

        int screenW = context.getScaledWindowWidth();
        int topY    = MARGIN;

        for (ActiveNotification active : notifications) {
            float slide   = easeOutCubic(active.slideProgress());
            float visible = active.visibleProgress();

            int xOffset = (int)((1f - slide) * (W + MARGIN));
            int x = screenW - W - MARGIN + xOffset;

            renderCard(context, font, active.notification(), x, topY, visible);
            topY += H + GAP;
        }
    }

    private static void renderCard(DrawContext context, TextRenderer font, Notification n,
                                   int x, int y, float visibleProgress) {
        context.fill(x, y, x + W, y + H, n.backgroundColor());
        context.fill(x, y, x + BORDER_W, y + H, n.borderColor());

        int textX = x + BORDER_W + PAD_H;
        context.drawText(font, truncate(font, n.title(), W - BORDER_W - PAD_H * 2 - 30),
                textX, y + PAD_V, n.titleColor(), false);

        int remainSecs = Math.max(0, Math.round((n.durationTicks() * visibleProgress) / 20f));
        String timerText = remainSecs + "s";
        int timerX = x + W - PAD_H - font.getWidth(timerText);
        context.drawText(font, timerText, timerX, y + PAD_V, n.titleColor(), false);

        context.drawText(font, truncate(font, n.description(), W - BORDER_W - PAD_H * 2),
                textX, y + PAD_V + font.fontHeight + 3, n.descriptionColor(), false);

        int barY = y + H - BAR_H;
        context.fill(x, barY, x + W, y + H, darken(n.borderColor(), 0.35f));

        int barFill = (int)(W * visibleProgress);
        if (barFill > 0) {
            context.fill(x, barY, x + barFill, y + H, n.borderColor());
        }
    }

    private static String truncate(TextRenderer font, String text, int maxWidth) {
        if (font.getWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        int limit = maxWidth - font.getWidth(ellipsis);
        return font.trimToWidth(text, limit) + ellipsis;
    }

    private static float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1.0 - t, 3.0);
    }

    private static int darken(int argb, float factor) {
        int a  = (argb >> 24) & 0xFF;
        int r  = (int)(((argb >> 16) & 0xFF) * factor);
        int gv = (int)(((argb >>  8) & 0xFF) * factor);
        int b  = (int)( (argb        & 0xFF) * factor);
        return (a << 24) | (r << 16) | (gv << 8) | b;
    }
}
