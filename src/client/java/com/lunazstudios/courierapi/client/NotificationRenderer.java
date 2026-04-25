package com.lunazstudios.courierapi.client;

import com.lunazstudios.courierapi.api.Notification;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * Renders active notifications as stacked cards in the top-right corner of the HUD.
 *
 * <p>Each card contains:
 * <ul>
 *   <li>A colored left accent border.</li>
 *   <li>The notification title and a countdown timer (top row).</li>
 *   <li>The description text — wraps to a second line if needed, with a {@code -} continuation marker.</li>
 *   <li>A progress bar at the bottom that drains as time runs out.</li>
 * </ul>
 *
 * <p>Cards slide in and out from the right edge using an ease-out cubic curve.
 * Rendering is skipped entirely when the HUD is hidden ({@code F1} mode).
 */
public final class NotificationRenderer {

    private static final int W        = 210;
    private static final int MARGIN   = 6;
    private static final int GAP      = 4;
    private static final int BORDER_W = 3;
    private static final int PAD_H    = 8;
    private static final int PAD_V    = 8;
    private static final int BAR_H    = 3;

    /** Card height when the description fits on one line. */
    private static final int H_ONE_LINE = 50;
    /** Card height when the description wraps to two lines. */
    private static final int H_TWO_LINE = 62;

    private NotificationRenderer() {}

    /** Renders all active notifications. Called every HUD render frame. */
    public static void render(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        Font font = mc.font;
        List<ActiveNotification> notifications = NotificationManager.getActive();
        if (notifications.isEmpty()) return;

        int screenW = graphics.guiWidth();
        int topY    = MARGIN;

        for (ActiveNotification active : notifications) {
            float slide   = easeOutCubic(active.slideProgress());
            float visible = active.visibleProgress();

            int xOffset = (int) ((1f - slide) * (W + MARGIN));
            int x = screenW - W - MARGIN + xOffset;

            int cardH = renderCard(graphics, font, active.notification(), x, topY, visible);
            topY += cardH + GAP;
        }
    }

    /**
     * Renders a single notification card and returns the height it occupied.
     * The height varies between {@link #H_ONE_LINE} and {@link #H_TWO_LINE}
     * depending on whether the description wraps.
     */
    private static int renderCard(GuiGraphicsExtractor graphics, Font font, Notification n,
                                  int x, int y, float visibleProgress) {
        int descMaxWidth = W - BORDER_W - PAD_H * 2;
        String[] descLines = wrapDescription(font, n.description(), descMaxWidth);
        int cardH = descLines.length > 1 ? H_TWO_LINE : H_ONE_LINE;

        // Background and left accent border
        graphics.fill(x, y, x + W, y + cardH, n.backgroundColor());
        graphics.fill(x, y, x + BORDER_W, y + cardH, n.borderColor());

        // Title + countdown timer on the same row
        int textX = x + BORDER_W + PAD_H;
        graphics.text(
                font,
                truncate(font, n.title(), W - BORDER_W - PAD_H * 2 - 30),
                textX,
                y + PAD_V,
                n.titleColor(),
                false
        );

        int remainSecs = Math.max(0, Math.round((n.durationTicks() * visibleProgress) / 20f));
        String timerText = remainSecs + "s";
        graphics.text(
                font,
                timerText,
                x + W - PAD_H - font.width(timerText),
                y + PAD_V,
                n.titleColor(),
                false
        );

        // Description — one or two lines
        int descY = y + PAD_V + font.lineHeight + 3;
        graphics.text(font, descLines[0], textX, descY, n.descriptionColor(), false);

        if (descLines.length > 1) {
            graphics.text(
                    font,
                    descLines[1],
                    textX,
                    descY + font.lineHeight + 2,
                    n.descriptionColor(),
                    false
            );
        }

        // Progress bar
        int barY = y + cardH - BAR_H;
        graphics.fill(x, barY, x + W, y + cardH, darken(n.borderColor(), 0.35f));

        int barFill = (int) (W * visibleProgress);
        if (barFill > 0) {
            graphics.fill(x, barY, x + barFill, y + cardH, n.borderColor());
        }

        return cardH;
    }

    /**
     * Splits {@code text} into at most two display lines for the given {@code maxWidth}.
     *
     * <p>If the text fits on one line it is returned as-is.
     * Otherwise the first line is broken at the last word boundary that fits,
     * a {@code -} continuation marker is appended, and the remainder becomes the second line
     * (truncated with {@code ...} if still too long).
     *
     * @return a 1- or 2-element array of ready-to-render strings
     */
    private static String[] wrapDescription(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return new String[]{ text };
        }

        // Find how much fits on line 1 with the "-" marker reserved
        int hyphenWidth = font.width("-");
        String fitted   = font.plainSubstrByWidth(text, maxWidth - hyphenWidth);

        // Prefer breaking at a word boundary
        int lastSpace = fitted.lastIndexOf(' ');
        String line1  = lastSpace > 0 ? fitted.substring(0, lastSpace) : fitted;
        String line2  = text.substring(line1.length()).stripLeading();

        return new String[]{
                line1 + "-",
                truncate(font, line2, maxWidth)
        };
    }

    private static String truncate(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;

        String ellipsis = "...";
        int limit = maxWidth - font.width(ellipsis);

        return font.plainSubstrByWidth(text, limit) + ellipsis;
    }

    private static float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1.0 - t, 3.0);
    }

    private static int darken(int argb, float factor) {
        int a  = (argb >> 24) & 0xFF;
        int r  = (int) (((argb >> 16) & 0xFF) * factor);
        int gv = (int) (((argb >>  8) & 0xFF) * factor);
        int b  = (int) ( (argb        & 0xFF) * factor);

        return (a << 24) | (r << 16) | (gv << 8) | b;
    }
}