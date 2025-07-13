package com.example.functionalareas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

public class FAManager {
    public static final Map<String, FARegion> regions = new HashMap<>();
    public static final Map<UUID, BlockPos[]> editSelections = new HashMap<>();
    private static final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private static final Map<UUID, Set<String>> playersInRegions = new HashMap<>();
    private static final Map<UUID, BlockPos> lastValidPositions = new HashMap<>();

    private static File regionFile;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type REGION_LIST_TYPE = new TypeToken<List<FARegion>>() {}.getType();

    public static void load(ServerWorld world) {
        File dir = world.getServer().getSavePath(WorldSavePath.ROOT).resolve("functionalareas").toFile();
        if (!dir.exists()) dir.mkdirs();
        regionFile = new File(dir, "regions.json");

        if (regionFile.exists()) {
            try (FileReader reader = new FileReader(regionFile)) {
                List<FARegion> list = GSON.fromJson(reader, REGION_LIST_TYPE);
                regions.clear();
                for (FARegion region : list) {
                    regions.put(region.name(), region);
                }
                System.out.println("[FunctionalAreas] Loaded " + regions.size() + " regions.");
            } catch (Exception e) {
                System.err.println("[FunctionalAreas] Error loading regions: " + e.getMessage());
            }
        }
    }

    public static void save() {
        if (regionFile == null) return;
        try (FileWriter writer = new FileWriter(regionFile)) {
            GSON.toJson(regions.values(), writer);
        } catch (Exception e) {
            System.err.println("[FunctionalAreas] Error saving regions: " + e.getMessage());
        }
    }

    public static void startEditMode(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (editSelections.containsKey(id)) {
            player.sendMessage(Text.literal("§cYou are already in edit mode."), false);
            return;
        }

        editSelections.put(id, new BlockPos[2]);

        ItemStack[] invCopy = new ItemStack[player.getInventory().main.size()];
        for (int i = 0; i < invCopy.length; i++) {
            invCopy[i] = player.getInventory().main.get(i).copy();
        }
        savedInventories.put(id, invCopy);

        player.getInventory().clear();
        ItemStack selector = new ItemStack(Items.BREEZE_ROD);
        selector.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§dArea Selector"));
        player.getInventory().setStack(0, selector);

        player.sendMessage(Text.literal("§aEdit mode enabled. Left click = Point A, Right click = Point B."), false);
    }

    public static boolean isInEditMode(ServerPlayerEntity player) {
        return editSelections.containsKey(player.getUuid());
    }

    public static void cancelEditMode(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        editSelections.remove(id);
        ItemStack[] saved = savedInventories.remove(id);
        if (saved != null) {
            player.getInventory().clear();
            for (int i = 0; i < saved.length; i++) {
                player.getInventory().setStack(i, saved[i]);
            }
            player.sendMessage(Text.literal("§cEdit mode cancelled. Inventory restored."), false);
        } else {
            player.sendMessage(Text.literal("§7You were not in edit mode."), false);
        }
    }

    public static void handlePlayerDisconnect(ServerPlayerEntity player) {
        if (isInEditMode(player)) cancelEditMode(player);
    }

    public static void selectPoint(ServerPlayerEntity player, BlockPos pos, boolean isLeftClick) {
        BlockPos[] pts = editSelections.get(player.getUuid());
        if (pts == null) return;

        if (isLeftClick) {
            pts[0] = pos;
            player.sendMessage(Text.literal("§7Point A selected: " + pos.toShortString()), false);
        } else {
            pts[1] = pos;
            player.sendMessage(Text.literal("§7Point B selected: " + pos.toShortString()), false);
            FAEffects.showCube((ServerWorld) player.getWorld(), pts[0], pts[1]);
            player.sendMessage(Text.literal("§eArea ready. Use /faset <name> <command> <hologram> to create."), false);
        }
    }

    public static void finishRegionCreation(ServerPlayerEntity player, String name, String command, String hologramRaw) {
        BlockPos[] pts = editSelections.remove(player.getUuid());
        ItemStack[] saved = savedInventories.remove(player.getUuid());
        if (pts == null || pts[0] == null || pts[1] == null) {
            player.sendMessage(Text.literal("§cSelect two points before creating a region."), false);
            return;
        }

        String hologram = hologramRaw.replace('&', '§');
        regions.put(name, new FARegion(name, pts[0], pts[1], command, hologram, ""));
        save();
        player.sendMessage(Text.literal("§aRegion '" + name + "' created."), false);

        if (saved != null) {
            player.getInventory().clear();
            for (int i = 0; i < saved.length; i++) {
                player.getInventory().setStack(i, saved[i]);
            }
        }
    }

    public static void listRegions(ServerPlayerEntity player) {
        if (regions.isEmpty()) {
            player.sendMessage(Text.literal("§7No regions registered."), false);
            return;
        }
        regions.values().forEach(r -> player.sendMessage(Text.literal(r.getDisplay()), false));
    }

    public static void deleteRegion(ServerPlayerEntity player, String name) {
        if (regions.remove(name) != null) {
            save();
            player.sendMessage(Text.literal("§cRegion '" + name + "' deleted."), false);
        } else {
            player.sendMessage(Text.literal("§cNo region found with that name."), false);
        }
    }

    public static void checkPlayerInRegions(ServerPlayerEntity player) {
        UUID pid = player.getUuid();
        Set<String> wasIn = playersInRegions.getOrDefault(pid, Collections.emptySet());
        Set<String> nowIn = new HashSet<>();

        BlockPos pos = player.getBlockPos();
        if (!isInsideAnyRegion(pos)) {
            lastValidPositions.put(pid, pos);
        }

        for (FARegion region : regions.values()) {
            if (region.isInside(pos)) {
                nowIn.add(region.name());

                if (!wasIn.contains(region.name())) {
                    String requiredPerm = region.requiredPermission();
                    if (requiredPerm != null && !requiredPerm.isEmpty()) {
                        if (!Permissions.check(player, requiredPerm, 2)) {
                            player.sendMessage(Text.literal("§cYou do not have permission to enter this area."), false);
                            BlockPos safe = lastValidPositions.getOrDefault(pid, pos);
                            player.teleport(player.getServerWorld(), safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5,
                                    player.getYaw(), player.getPitch());
                            return;
                        }
                    }

                    if (!region.command().isEmpty()) {
                        player.getServer().getCommandManager()
                                .executeWithPrefix(player.getServer().getCommandSource(), region.command());
                    }

                    if (!region.hologram().isEmpty()) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(region.hologram())));
                        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 60, 10));
                    }
                }
            }
        }

        if (nowIn.isEmpty()) {
            playersInRegions.remove(pid);
        } else {
            playersInRegions.put(pid, nowIn);
        }
    }

    private static boolean isInsideAnyRegion(BlockPos pos) {
        for (FARegion region : regions.values()) {
            if (region.isInside(pos)) return true;
        }
        return false;
    }

    public static boolean canPassThrough(ServerPlayerEntity player, BlockPos pos) {
        for (FARegion region : regions.values()) {
            if (region.isInside(pos)) {
                String perm = region.requiredPermission();
                if (perm != null && !perm.isEmpty()) {
                    if (!Permissions.check(player, perm, 2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
