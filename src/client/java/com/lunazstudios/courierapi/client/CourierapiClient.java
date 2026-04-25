package com.lunazstudios.courierapi.client;

import com.lunazstudios.courierapi.Courierapi;
import com.lunazstudios.courierapi.network.NotificationPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.util.Identifier;

public class CourierapiClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> NotificationManager.tick());

        HudElementRegistry.addLast(
                Identifier.of(Courierapi.MODID, "notifications"),
                (context, tickCounter) -> NotificationRenderer.render(context, tickCounter.getTickProgress(true)));

        ClientPlayNetworking.registerGlobalReceiver(NotificationPacket.ID, (payload, context) ->
                context.client().execute(() -> NotificationManager.queue(payload.notification())));
    }
}
