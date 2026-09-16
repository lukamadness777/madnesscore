package dev.lukamadness.madnesscore.neoforge.event.villager;

import dev.lukamadness.madnesscore.common.event.villager.VillagerHeritageHandler;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class VillagerNeoForgeEvents {
    private VillagerNeoForgeEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(VillagerNeoForgeEvents::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(VillagerNeoForgeEvents::onLivingDeath);
    }

    private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Villager villager) {
            if (villager.isBaby()) {
                VillagerHeritageHandler.onVillagerChildSpawn(villager);
            } else {
                VillagerHeritageHandler.onWildVillagerSpawn(villager);
            }
        }
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            VillagerHeritageHandler.onVillagerDeath(villager);
        }
    }
}
