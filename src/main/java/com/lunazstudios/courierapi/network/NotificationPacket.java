package com.lunazstudios.courierapi.network;

import com.lunazstudios.courierapi.Courierapi;
import com.lunazstudios.courierapi.api.Notification;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record NotificationPacket(Notification notification) implements CustomPacketPayload {

    public static final Type<NotificationPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Courierapi.MODID, "notification"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NotificationPacket> CODEC =
            StreamCodec.composite(
                    Notification.STREAM_CODEC,
                    NotificationPacket::notification,
                    NotificationPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}