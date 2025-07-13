package com.example.functionalareas;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

public class FAEffects {

    public static void showCube(ServerWorld world, BlockPos pos1, BlockPos pos2) {
        if (pos1 == null || pos2 == null) return;

        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());

        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // Partícula de color azul celeste con escala 1.0
        DustParticleEffect particle = new DustParticleEffect(new Vector3f(0.2f, 0.7f, 1.0f), 1.0f);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                world.spawnParticles(particle, x + 0.5, y + 0.5, minZ + 0.5, 1, 0, 0, 0, 0);
                world.spawnParticles(particle, x + 0.5, y + 0.5, maxZ + 0.5, 1, 0, 0, 0, 0);
            }
        }

        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                world.spawnParticles(particle, minX + 0.5, y + 0.5, z + 0.5, 1, 0, 0, 0, 0);
                world.spawnParticles(particle, maxX + 0.5, y + 0.5, z + 0.5, 1, 0, 0, 0, 0);
            }
        }

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.spawnParticles(particle, x + 0.5, minY + 0.5, z + 0.5, 1, 0, 0, 0, 0);
                world.spawnParticles(particle, x + 0.5, maxY + 0.5, z + 0.5, 1, 0, 0, 0, 0);
            }
        }
    }
}
