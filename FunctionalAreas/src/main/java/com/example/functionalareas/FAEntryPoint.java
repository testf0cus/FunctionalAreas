package com.example.functionalareas;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.world.ServerWorld;

public class FAEntryPoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        System.out.println("[FunctionalAreas] Mod initializing...");

        // Registrar Projectiles
        FAProjectileTracker.register();

        // Registrar comandos
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            FACommandHandler.register(dispatcher);
        });

        // Registrar eventos del jugador (clics, protección, etc.)
        FAEventHandler.register();

        // Cargar regiones al iniciar el servidor
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld world = server.getOverworld();
            if (world != null) {
                FAManager.load(world);
            }
        });

        // Guardar regiones al cerrar el servidor
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            FAManager.save();
        });

        // Opcional: limpieza final
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            // Limpiar estructuras en memoria si fuera necesario
        });

    }
}
