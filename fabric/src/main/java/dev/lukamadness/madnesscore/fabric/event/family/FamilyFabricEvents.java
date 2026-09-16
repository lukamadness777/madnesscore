package dev.lukamadness.madnesscore.fabric.event.family;

import dev.lukamadness.madnesscore.common.event.family.FamilyDeathHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public final class FamilyFabricEvents {
    private FamilyFabricEvents() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                FamilyDeathHandler.onPlayerDeath(player);
            }
        });
    }
}
