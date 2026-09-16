package dev.lukamadness.madnesscore.fabric.event.bloodline;

import dev.lukamadness.madnesscore.common.event.bloodline.BloodlineDeathHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public final class BloodlineFabricEvents {
    private BloodlineFabricEvents() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                BloodlineDeathHandler.onPlayerDeath(player);
            }
        });
    }
}
