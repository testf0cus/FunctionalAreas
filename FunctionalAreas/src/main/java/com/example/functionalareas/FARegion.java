package com.example.functionalareas;

import net.minecraft.util.math.BlockPos;

public record FARegion(String name, BlockPos pos1, BlockPos pos2, String command, String hologram, String requiredPermission) {

    public boolean isInside(BlockPos pos) {
        int x1 = Math.min(pos1.getX(), pos2.getX());
        int y1 = Math.min(pos1.getY(), pos2.getY());
        int z1 = Math.min(pos1.getZ(), pos2.getZ());
        int x2 = Math.max(pos1.getX(), pos2.getX());
        int y2 = Math.max(pos1.getY(), pos2.getY());
        int z2 = Math.max(pos1.getZ(), pos2.getZ());

        return pos.getX() >= x1 && pos.getX() <= x2 &&
                pos.getY() >= y1 && pos.getY() <= y2 &&
                pos.getZ() >= z1 && pos.getZ() <= z2;
    }

    public String getDisplay() {
        return String.format("§a%s§r - [%d, %d, %d] to [%d, %d, %d] -> /%s | Hologram: %s | Perm: %s",
                name, pos1.getX(), pos1.getY(), pos1.getZ(),
                pos2.getX(), pos2.getY(), pos2.getZ(), command,
                hologram != null ? hologram : "none",
                requiredPermission != null ? requiredPermission : "none");
    }
}
