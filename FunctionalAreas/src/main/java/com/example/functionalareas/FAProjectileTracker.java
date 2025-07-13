package com.example.functionalareas;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

public class FAProjectileTracker {

    public static void register() {
        // Revisión por tick de perlas dentro de regiones definidas
        ServerTickEvents.START_SERVER_TICK.register((MinecraftServer server) -> {
            for (ServerWorld world : server.getWorlds()) {
                for (FARegion region : FAManager.getRegions().values()) {
                    Box regionBox = new Box(region.pos1(), region.pos2()).expand(1);

                    for (EnderPearlEntity pearl : world.getEntitiesByClass(
                            EnderPearlEntity.class,
                            regionBox,
                            p -> p.getOwner() instanceof ServerPlayerEntity)) {

                        ServerPlayerEntity player = (ServerPlayerEntity) pearl.getOwner();
                        if (player != null) {
                            BlockPos pos = pearl.getBlockPos();
                            if (!FAManager.canPassThrough(player, pos)) {
                                pearl.discard();
                                player.sendMessage(Text.literal("§cYour ender pearl was blocked by an area restriction."), false);
                            }
                        }
                    }
                }
            }
        });
    }
}
