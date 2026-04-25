package com.lunazstudios.courierapi;

import com.lunazstudios.courierapi.network.PacketHandler;
import com.lunazstudios.courierapi.server.JsonNotificationWatcher;
import com.lunazstudios.courierapi.server.NotificationDefinitions;
import com.lunazstudios.courierapi.command.CourierCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Courierapi implements ModInitializer {

    public static final String MODID  = "courierapi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        PacketHandler.register();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> NotificationDefinitions.reload(server));
        ServerTickEvents.END_SERVER_TICK.register(server -> JsonNotificationWatcher.onServerTick(server));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CourierCommand.register(dispatcher));
    }
}
