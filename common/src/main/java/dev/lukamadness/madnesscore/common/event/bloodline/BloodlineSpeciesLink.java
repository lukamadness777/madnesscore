package dev.lukamadness.madnesscore.common.event.bloodline;

import dev.lukamadness.madnesscore.common.api.bloodline.BloodlineApi;
import dev.lukamadness.madnesscore.common.api.bloodline.BloodlineInstance;
import dev.lukamadness.madnesscore.common.api.species.Species;
import dev.lukamadness.madnesscore.common.api.species.SpeciesApi;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public final class BloodlineSpeciesLink {
    private BloodlineSpeciesLink() {}

    public static void init() {
        SpeciesApi.onSpeciesChange(BloodlineSpeciesLink::onSpeciesChanged);
    }

    private static void onSpeciesChanged(MinecraftServer server, UUID uuid, Species oldSpecies, Species newSpecies) {
        for (BloodlineInstance instance : BloodlineApi.getBloodlines(server, uuid)) {
            if (!instance.bloodline().isCompatibleWith(newSpecies)) {
                BloodlineApi.removeBloodline(server, uuid, instance.bloodline().id());
            }
        }
    }
}
