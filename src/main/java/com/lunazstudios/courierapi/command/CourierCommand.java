package com.lunazstudios.courierapi.command;

import com.lunazstudios.courierapi.api.CourierAPI;
import com.lunazstudios.courierapi.api.Notification;
import com.lunazstudios.courierapi.server.NotificationDefinitions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.Optional;

public final class CourierCommand {

    private CourierCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("courier")
                        .requires(source -> source.permissions().hasPermission(
                                new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)
                        ))

                        .then(Commands.literal("send")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            NotificationDefinitions.getIds().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            String id = StringArgumentType.getString(ctx, "id");
                                            Optional<Notification> opt = NotificationDefinitions.get(id);

                                            if (opt.isEmpty()) {
                                                ctx.getSource().sendFailure(Component.literal(
                                                        "[CourierAPI] ID '" + id + "' not found. "
                                                                + "Check couriernotifications.json or run /courier reload."));
                                                return 0;
                                            }

                                            CourierAPI.broadcast(ctx.getSource().getServer(), opt.get());
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "[CourierAPI] Notification '" + id + "' sent to all online players."), true);
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    NotificationDefinitions.reload(ctx.getSource().getServer());
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "[CourierAPI] couriernotifications.json reloaded."), true);
                                    return 1;
                                })
                        )
        );
    }
}