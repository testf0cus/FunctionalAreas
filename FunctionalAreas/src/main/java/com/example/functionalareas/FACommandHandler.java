package com.example.functionalareas;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class FACommandHandler {

    public static void register(com.mojang.brigadier.CommandDispatcher<ServerCommandSource> dispatcher) {

        // /fahelp
        dispatcher.register(CommandManager.literal("fahelp")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    source.sendFeedback(() -> Text.literal("§6§l   Functional Areas Help   "), false);
                    source.sendFeedback(() -> Text.literal("§b/faset §7– Enter edit mode"), false);
                    source.sendFeedback(() -> Text.literal("§b/faset §f<name> [command] [#hologram] §7– Create region"), false);
                    source.sendFeedback(() -> Text.literal("§b/fasetperm §f<name> <perm> §7– set custom restriction"), false);
                    source.sendFeedback(() -> Text.literal("§b/faview §7– List all regions"), false);
                    source.sendFeedback(() -> Text.literal("§b/fadelete §<name> §7– Delete a region"), false);
                    source.sendFeedback(() -> Text.literal("§b/facancel §7– Cancel edit mode"), false);
                    source.sendFeedback(() -> Text.literal("§b/fahelp §7– Show this help"), false);
                    return 1;
                }));

        // /faset
        dispatcher.register(CommandManager.literal("faset")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    if (!Permissions.check(source, "functionalareas.use", 2)) {
                        source.sendFeedback(() -> Text.literal("§cYou don't have permission to use Functional Areas."), false);
                        return 0;
                    }

                    if (!source.isExecutedByPlayer()) {
                        source.sendFeedback(() -> Text.literal("§cThis command can only be used by a player."), false);
                        return 0;
                    }

                    ServerPlayerEntity player = source.getPlayer();
                    if (FAManager.isInEditMode(player)) {
                        player.sendMessage(Text.literal("§cYou are already in edit mode."), false);
                        return 0;
                    }

                    FAManager.startEditMode(player);
                    return 1;
                })
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .then(CommandManager.argument("args", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerCommandSource source = ctx.getSource();
                                    if (!Permissions.check(source, "functionalareas.use", 2)) {
                                        source.sendFeedback(() -> Text.literal("§cYou don't have permission to use Functional Areas."), false);
                                        return 0;
                                    }

                                    ServerPlayerEntity player = source.getPlayer();
                                    String name = StringArgumentType.getString(ctx, "name");
                                    String allArgs = StringArgumentType.getString(ctx, "args");

                                    String command = "";
                                    String hologram = "";

                                    String[] parts = allArgs.split(" ");
                                    StringBuilder commandBuilder = new StringBuilder();
                                    StringBuilder hologramBuilder = new StringBuilder();

                                    boolean inHologram = false;
                                    for (String part : parts) {
                                        if (!inHologram && part.startsWith("#")) {
                                            inHologram = true;
                                            hologramBuilder.append(part.substring(1)).append(" ");
                                        } else if (inHologram) {
                                            hologramBuilder.append(part).append(" ");
                                        } else {
                                            commandBuilder.append(part).append(" ");
                                        }
                                    }

                                    command = commandBuilder.toString().trim();
                                    hologram = hologramBuilder.toString().trim().replace('&', '§');

                                    FAManager.finishRegionCreation(player, name, command, hologram);
                                    return 1;
                                }))));

        // /facancel
        dispatcher.register(CommandManager.literal("facancel")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    if (!Permissions.check(source, "functionalareas.use", 2)) {
                        source.sendFeedback(() -> Text.literal("§cYou don't have permission to use Functional Areas."), false);
                        return 0;
                    }

                    ServerPlayerEntity player = source.getPlayer();
                    FAManager.cancelEditMode(player);
                    return 1;
                }));

        // /fadelete
        dispatcher.register(CommandManager.literal("fadelete")
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerCommandSource source = ctx.getSource();
                            if (!Permissions.check(source, "functionalareas.use", 2)) {
                                source.sendFeedback(() -> Text.literal("§cYou don't have permission to use Functional Areas."), false);
                                return 0;
                            }

                            ServerPlayerEntity player = source.getPlayer();
                            String name = StringArgumentType.getString(ctx, "name");
                            FAManager.deleteRegion(player, name);
                            return 1;
                        })));

        // /faview
        dispatcher.register(CommandManager.literal("faview")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    if (!Permissions.check(source, "functionalareas.use", 2)) {
                        source.sendFeedback(() -> Text.literal("§cYou don't have permission to use Functional Areas."), false);
                        return 0;
                    }

                    ServerPlayerEntity player = source.getPlayer();
                    FAManager.listRegions(player);
                    return 1;
                }));

        // /fasetperm <region> <permission>
        dispatcher.register(CommandManager.literal("fasetperm")
                .then(CommandManager.argument("region", StringArgumentType.word())
                        .then(CommandManager.argument("permission", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerCommandSource source = ctx.getSource();
                                    if (!Permissions.check(source, "functionalareas.use", 2)) {
                                        source.sendFeedback(() -> Text.literal("§cYou don't have permission to use Functional Areas."), false);
                                        return 0;
                                    }

                                    String regionName = StringArgumentType.getString(ctx, "region");
                                    String perm = StringArgumentType.getString(ctx, "permission");

                                    FARegion region = FAManager.regions.get(regionName);
                                    if (region == null) {
                                        source.sendFeedback(() -> Text.literal("§cRegion '" + regionName + "' not found."), false);
                                        return 0;
                                    }

                                    String finalPerm = perm.equalsIgnoreCase("none") ? "" : perm;
                                    FARegion updated = new FARegion(
                                            region.name(),
                                            region.pos1(),
                                            region.pos2(),
                                            region.command(),
                                            region.hologram(),
                                            finalPerm
                                    );

                                    FAManager.regions.put(regionName, updated);
                                    FAManager.save();

                                    source.sendFeedback(() -> Text.literal("§aPermission for region '" + regionName + "' set to: "
                                            + (finalPerm.isEmpty() ? "§7none" : finalPerm)), false);
                                    return 1;
                                }))));
    }
}
