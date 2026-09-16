package dev.lukamadness.madnesscore.common.event.family;

import dev.lukamadness.madnesscore.common.api.bloodline.Bloodline;
import dev.lukamadness.madnesscore.common.api.bloodline.BloodlineApi;
import dev.lukamadness.madnesscore.common.api.bloodline.BloodlineChangeListener;
import dev.lukamadness.madnesscore.common.api.family.Family;
import dev.lukamadness.madnesscore.common.api.family.FamilyApi;
import dev.lukamadness.madnesscore.common.api.family.FamilyInstance;
import dev.lukamadness.madnesscore.common.api.species.Species;
import dev.lukamadness.madnesscore.common.api.species.SpeciesApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public final class FamilyRequirementLink {
    private FamilyRequirementLink() {}

    public static void init() {
        SpeciesApi.onSpeciesChange((server, uuid, oldSpecies, newSpecies) -> revalidateAll(server, uuid));

        BloodlineApi.onBloodlineChange(new BloodlineChangeListener() {
            @Override
            public void onBloodlineAdded(MinecraftServer server, UUID uuid, Bloodline bloodline, double percentage) {
                revalidateAll(server, uuid);
            }

            @Override
            public void onBloodlineRemoved(MinecraftServer server, UUID uuid, Bloodline bloodline) {
                revalidateAll(server, uuid);
            }
        });
    }

    private static void revalidateAll(MinecraftServer server, UUID uuid) {
        Species species = SpeciesApi.getSpecies(server, uuid);

        for (FamilyInstance instance : FamilyApi.getFamilies(server, uuid)) {
            Family family = instance.family();
            boolean meetsRequirements = meetsRequirements(server, uuid, family, species);

            if (meetsRequirements) {
                if (instance.disabled()) {
                    FamilyApi.setFamilyDisabled(server, uuid, family.id(), false);
                }
                continue;
            }

            switch (family.onRequirementLost()) {
                case PRESERVE -> {}
                case REMOVE -> FamilyApi.removeFamily(server, uuid, family.id());
                case DISABLE -> {
                    if (!instance.disabled()) {
                        FamilyApi.setFamilyDisabled(server, uuid, family.id(), true);
                    }
                }
            }
        }
    }

    private static boolean meetsRequirements(MinecraftServer server, UUID uuid, Family family, Species species) {
        if (!family.isCompatibleWith(species)) {
            return false;
        }
        ResourceLocation requiredBloodline = family.requiredBloodline().orElse(null);
        return requiredBloodline == null || BloodlineApi.isBloodline(server, uuid, requiredBloodline);
    }
}
