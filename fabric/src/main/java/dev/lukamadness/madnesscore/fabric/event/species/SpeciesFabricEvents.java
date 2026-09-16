package dev.lukamadness.madnesscore.fabric.event.species;

import dev.lukamadness.madnesscore.common.event.species.SpeciesDeathHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public final class SpeciesFabricEvents {
    private SpeciesFabricEvents() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                SpeciesDeathHandler.onPlayerDeath(player);
            }
        });
    }
}
