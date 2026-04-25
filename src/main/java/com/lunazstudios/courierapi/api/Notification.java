package com.lunazstudios.courierapi.api;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/**
 * Immutable data object representing a HUD notification.
 *
 * <p>Use the fluent {@link Builder} to construct instances:
 * <pre>{@code
 * Notification n = Notification.builder("Server Restart", "The server restarts in 5 minutes.")
 *         .durationSeconds(8)
 *         .titleColor(0xFFFFFF55)
 *         .borderColor(0xFFFF4444)
 *         .build();
 * }</pre>
 *
 * <p>Colors are ARGB integers (e.g. {@code 0xFFFFFF55} = fully opaque yellow).
 * If the alpha component is omitted (6-digit hex), full opacity is assumed.
 */
public record Notification(
        String title,
        String description,
        int durationTicks,
        int titleColor,
        int descriptionColor,
        int backgroundColor,
        int borderColor
) {
    /** Default title color: opaque yellow ({@code #FFFF55}). */
    public static final int DEFAULT_TITLE_COLOR       = 0xFFFFFF55;
    /** Default description color: opaque white ({@code #FFFFFF}). */
    public static final int DEFAULT_DESCRIPTION_COLOR = 0xFFFFFFFF;
    /** Default background color: 80% opaque black ({@code #CC000000}). */
    public static final int DEFAULT_BACKGROUND_COLOR  = 0xCC000000;
    /** Default border color: opaque yellow ({@code #FFFF55}). */
    public static final int DEFAULT_BORDER_COLOR      = 0xFFFFFF55;

    /** Internal codec used to serialize/deserialize this record over the network. */
    public static final PacketCodec<ByteBuf, Notification> PACKET_CODEC = new PacketCodec<>() {
        @Override
        public Notification decode(ByteBuf buf) {
            String title            = PacketCodecs.STRING.decode(buf);
            String description      = PacketCodecs.STRING.decode(buf);
            int    durationTicks    = PacketCodecs.VAR_INT.decode(buf);
            int    titleColor       = buf.readInt();
            int    descriptionColor = buf.readInt();
            int    backgroundColor  = buf.readInt();
            int    borderColor      = buf.readInt();
            return new Notification(title, description, durationTicks,
                    titleColor, descriptionColor, backgroundColor, borderColor);
        }

        @Override
        public void encode(ByteBuf buf, Notification n) {
            PacketCodecs.STRING.encode(buf, n.title());
            PacketCodecs.STRING.encode(buf, n.description());
            PacketCodecs.VAR_INT.encode(buf, n.durationTicks());
            buf.writeInt(n.titleColor());
            buf.writeInt(n.descriptionColor());
            buf.writeInt(n.backgroundColor());
            buf.writeInt(n.borderColor());
        }
    };

    /**
     * Returns a new {@link Builder} with the required title and description.
     *
     * @param title       the notification title (short, displayed prominently)
     * @param description the notification body text
     */
    public static Builder builder(String title, String description) {
        return new Builder(title, description);
    }

    /**
     * Fluent builder for {@link Notification}.
     *
     * <p>All color and duration fields are optional and fall back to their defaults.
     */
    public static final class Builder {
        private final String title;
        private final String description;
        private int durationTicks    = 100;
        private int titleColor       = DEFAULT_TITLE_COLOR;
        private int descriptionColor = DEFAULT_DESCRIPTION_COLOR;
        private int backgroundColor  = DEFAULT_BACKGROUND_COLOR;
        private int borderColor      = DEFAULT_BORDER_COLOR;

        private Builder(String title, String description) {
            this.title       = title;
            this.description = description;
        }

        /** Sets the display duration in ticks (20 ticks = 1 second). */
        public Builder duration(int ticks) {
            this.durationTicks = ticks;
            return this;
        }

        /** Sets the display duration in seconds (converted to ticks automatically). */
        public Builder durationSeconds(float seconds) {
            this.durationTicks = Math.max(1, (int)(seconds * 20));
            return this;
        }

        /** Sets the title text color as an ARGB integer. */
        public Builder titleColor(int color) {
            this.titleColor = color;
            return this;
        }

        /** Sets the description text color as an ARGB integer. */
        public Builder descriptionColor(int color) {
            this.descriptionColor = color;
            return this;
        }

        /** Sets the card background color as an ARGB integer. */
        public Builder backgroundColor(int color) {
            this.backgroundColor = color;
            return this;
        }

        /** Sets the left accent border and progress bar color as an ARGB integer. */
        public Builder borderColor(int color) {
            this.borderColor = color;
            return this;
        }

        /** Builds and returns the immutable {@link Notification}. */
        public Notification build() {
            return new Notification(title, description, durationTicks,
                    titleColor, descriptionColor, backgroundColor, borderColor);
        }
    }
}
