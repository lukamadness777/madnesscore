package dev.lukamadness.madnesscore.neoforge.event.family;

import dev.lukamadness.madnesscore.common.event.family.FamilyDeathHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class FamilyNeoForgeEvents {
    private FamilyNeoForgeEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FamilyNeoForgeEvents::onLivingDeath);
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FamilyDeathHandler.onPlayerDeath(player);
        }
    }
}
