package dev.lukamadness.madnesscore.neoforge.event.species;

import dev.lukamadness.madnesscore.common.event.species.SpeciesDeathHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class SpeciesNeoForgeEvents {
    private SpeciesNeoForgeEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SpeciesNeoForgeEvents::onLivingDeath);
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SpeciesDeathHandler.onPlayerDeath(player);
        }
    }
}
