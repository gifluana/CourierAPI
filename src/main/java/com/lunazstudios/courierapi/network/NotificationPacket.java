package com.lunazstudios.courierapi.network;

import com.lunazstudios.courierapi.Courierapi;
import com.lunazstudios.courierapi.api.Notification;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** S2C packet that carries a {@link Notification} from the server to a client. */
public record NotificationPacket(Notification notification) implements CustomPayload {

    public static final CustomPayload.Id<NotificationPacket> ID =
            new CustomPayload.Id<>(Identifier.of(Courierapi.MODID, "notification"));

    public static final PacketCodec<RegistryByteBuf, NotificationPacket> CODEC =
            PacketCodec.tuple(
                    Notification.PACKET_CODEC.cast(),
                    NotificationPacket::notification,
                    NotificationPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
