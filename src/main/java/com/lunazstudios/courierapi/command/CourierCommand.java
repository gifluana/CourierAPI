package com.lunazstudios.courierapi.command;

import com.lunazstudios.courierapi.api.CourierAPI;
import com.lunazstudios.courierapi.api.Notification;
import com.lunazstudios.courierapi.server.NotificationDefinitions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionCheck;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Optional;

/**
 * Registers the {@code /courier} command tree (requires permission level 2).
 *
 * <ul>
 *   <li>{@code /courier send <id>} — broadcasts the notification with the given ID to all online players.</li>
 *   <li>{@code /courier reload}   — reloads {@code couriernotifications.json} from disk.</li>
 * </ul>
 */
public final class CourierCommand {

    private CourierCommand() {}

    /** Registers all subcommands into the given dispatcher. */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("courier")
                .requires(CommandManager.requirePermissionLevel(
                        new PermissionCheck.Require(new Permission.Level(PermissionLevel.GAMEMASTERS))))

                .then(CommandManager.literal("send")
                    .then(CommandManager.argument("id", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            NotificationDefinitions.getIds().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String id = StringArgumentType.getString(ctx, "id");
                            Optional<Notification> opt = NotificationDefinitions.get(id);

                            if (opt.isEmpty()) {
                                ctx.getSource().sendError(Text.literal(
                                        "[CourierAPI] ID '" + id + "' not found. "
                                        + "Check couriernotifications.json or run /courier reload."));
                                return 0;
                            }

                            CourierAPI.broadcast(ctx.getSource().getServer(), opt.get());
                            ctx.getSource().sendFeedback(() -> Text.literal(
                                    "[CourierAPI] Notification '" + id + "' sent to all online players."),
                                    true);
                            return 1;
                        })
                    )
                )

                .then(CommandManager.literal("reload")
                    .executes(ctx -> {
                        NotificationDefinitions.reload(ctx.getSource().getServer());
                        ctx.getSource().sendFeedback(() -> Text.literal(
                                "[CourierAPI] couriernotifications.json reloaded."),
                                true);
                        return 1;
                    })
                )
        );
    }
}
