package com.lunazstudios.courierapi.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/** Registers CourierAPI's network payload types. */
public final class PacketHandler {

    private PacketHandler() {}

    /** Registers the S2C {@link NotificationPacket} type. Called during mod initialization. */
    public static void register() {
        PayloadTypeRegistry.playS2C().register(NotificationPacket.ID, NotificationPacket.CODEC);
    }
}
