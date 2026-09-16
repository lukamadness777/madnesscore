package dev.lukamadness.madnesscore.neoforge.event.bloodline;

import dev.lukamadness.madnesscore.common.event.bloodline.BloodlineDeathHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class BloodlineNeoForgeEvents {
    private BloodlineNeoForgeEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BloodlineNeoForgeEvents::onLivingDeath);
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BloodlineDeathHandler.onPlayerDeath(player);
        }
    }
}
