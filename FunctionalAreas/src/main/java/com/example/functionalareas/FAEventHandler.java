package com.example.functionalareas;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class FAEventHandler {

    public static void register() {

        // Click izquierdo con Breeze Rod = marcar Punto A (sin romper bloque)
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            if (!FAManager.isInEditMode(serverPlayer)) return ActionResult.PASS;
            if (serverPlayer.getMainHandStack().getItem() != Items.BREEZE_ROD) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            FAManager.selectPoint(serverPlayer, pos, true);
            return ActionResult.SUCCESS;
        });

        // Click derecho con Breeze Rod = marcar Punto B
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            if (!(world instanceof ServerWorld)) return ActionResult.PASS;
            if (!FAManager.isInEditMode(serverPlayer)) return ActionResult.PASS;
            if (serverPlayer.getMainHandStack().getItem() != Items.BREEZE_ROD) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND || hitResult.getType() != BlockHitResult.Type.BLOCK) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            FAManager.selectPoint(serverPlayer, pos, false);
            return ActionResult.SUCCESS;
        });

        // Bloquear interacción con cofres durante edición
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            if (!FAManager.isInEditMode(serverPlayer)) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            if (world.getBlockEntity(pos) instanceof LootableContainerBlockEntity) {
                serverPlayer.sendMessage(Text.literal("§cYou can't use containers in edit mode."), false);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        // Cancelar modo edición si se desconecta
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            FAManager.handlePlayerDisconnect(handler.getPlayer());
        });

        // Protección reforzada del selector por tick
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!FAManager.isInEditMode(player)) continue;

                boolean found = false;
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack item = player.getInventory().getStack(i);
                    if (item.getItem() == Items.BREEZE_ROD) {
                        if (!found) {
                            if (i != 0) {
                                player.getInventory().removeStack(i);
                                player.getInventory().setStack(0, item.copy());
                                player.sendMessage(Text.literal("§cSelector returned to slot 1."), false);
                            }
                            found = true;
                        } else {
                            player.getInventory().removeStack(i);
                            player.sendMessage(Text.literal("§cDuplicated selector removed."), false);
                        }
                    }
                }

                if (!found || player.getInventory().getStack(0).isEmpty()) {
                    ItemStack selector = new ItemStack(Items.BREEZE_ROD);
                    selector.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§dArea Selector"));
                    player.getInventory().setStack(0, selector);
                }

                player.getWorld().getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(5), entity ->
                        entity.getOwner() == player && entity.getStack().getItem() == Items.BREEZE_ROD
                ).forEach(ItemEntity::discard);

                player.currentScreenHandler.sendContentUpdates();
            }

            // Verificar entrada a regiones para ejecutar comandos y mostrar hologramas
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                FAManager.checkPlayerInRegions(player);
            }
        });
    }
}
