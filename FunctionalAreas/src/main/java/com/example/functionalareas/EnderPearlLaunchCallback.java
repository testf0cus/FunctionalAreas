package com.example.functionalareas;

import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

@FunctionalInterface
public interface EnderPearlLaunchCallback {
    ActionResult onLaunch(EnderPearlEntity pearl, World world, ServerPlayerEntity player);
}
