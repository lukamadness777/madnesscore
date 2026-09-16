package dev.lukamadness.madnesscore.fabric.event.villager;

import dev.lukamadness.madnesscore.common.event.villager.VillagerHeritageHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.npc.Villager;

public final class VillagerFabricEvents {
    private VillagerFabricEvents() {}

    public static void register() {
        // Cubre tanto spawns naturales como crías recién nacidas (ambos entran por ENTITY_LOAD al
        // agregarse al mundo por primera vez). El handler decide internamente si es cría o no.
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Villager villager) {
                if (villager.isBaby()) {
                    VillagerHeritageHandler.onVillagerChildSpawn(villager);
                } else {
                    VillagerHeritageHandler.onWildVillagerSpawn(villager);
                }
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof Villager villager) {
                VillagerHeritageHandler.onVillagerDeath(villager);
            }
        });
    }
}
