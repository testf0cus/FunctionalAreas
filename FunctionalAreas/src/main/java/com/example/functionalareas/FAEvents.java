package com.example.functionalareas;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class FAEvents {
    public static final Event<EnderPearlLaunchCallback> ENDER_PEARL_LAUNCH =
            EventFactory.createArrayBacked(EnderPearlLaunchCallback.class,
                    (listeners) -> (pearl, world, player) -> {
                        for (EnderPearlLaunchCallback listener : listeners) {
                            ActionResult result = listener.onLaunch(pearl, world, player);
                            if (result != ActionResult.PASS) {
                                return result;
                            }
                        }
                        return ActionResult.PASS;
                    }
            );
}
