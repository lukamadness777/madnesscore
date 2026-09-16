package dev.lukamadness.madnesscore.common.event.villager;

import dev.lukamadness.madnesscore.common.api.bloodline.Bloodline;
import dev.lukamadness.madnesscore.common.api.bloodline.BloodlineApi;
import dev.lukamadness.madnesscore.common.api.bloodline.BloodlineInstance;
import dev.lukamadness.madnesscore.common.api.species.Species;
import dev.lukamadness.madnesscore.common.api.species.SpeciesApi;
import dev.lukamadness.madnesscore.common.api.species.SpeciesRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Aplica el sistema de Species/Bloodlines a los aldeanos:
 * - Aldeano "salvaje" (spawn natural, sin padres conocidos): species aleatoria, restringida a isHumanLike.
 * - Cría (nace de dos aldeanos): mezcla al 50/50 la herencia genética de ambos padres.
 * - Muerte: NO usa el reset con gamerule de los jugadores; directamente purga sus datos.
 *
 * Nota: una species pura no-humana (ej. "saiyan") solo se diluye correctamente en una cría mixta si
 * existe un Bloodline registrado con el MISMO ResourceLocation que esa Species, y con heritable = true.
 * Si no existe ese Bloodline gemelo, ese "gen" simplemente se pierde en la mezcla (no rompe nada, pero
 * conviene que los mods de contenido registren siempre el par Species + Bloodline para razas puras).
 */
public final class VillagerHeritageHandler {
    /** Radio (en bloques) donde se buscan los posibles padres de una cría recién nacida. */
    private static final double PARENT_SEARCH_RADIUS = 4.0;

    private VillagerHeritageHandler() {}

    /** Aldeano "salvaje": spawn natural de aldea, conversión desde otra fuente sin padres conocidos, etc. */
    public static void onWildVillagerSpawn(Villager villager) {
        if (!(villager.level() instanceof ServerLevel level)) return;
        MinecraftServer server = level.getServer();
        if (server == null) return;

        UUID uuid = villager.getUUID();
        if (SpeciesApi.hasStoredSpecies(server, uuid)) return; // ya tiene datos, no pisar

        ResourceLocation speciesId = SpeciesRegistry.rollAnyFiltered(Species::isHumanLike);
        SpeciesApi.setSpecies(server, uuid, speciesId);
    }

    /**
     * Heurístico de emparentado: cuando nace una cría de aldeano, vanilla no expone los dos padres
     * directamente (Villager no extiende Animal). Se buscan los 2 aldeanos adultos más cercanos a la
     * cría en el momento en que aparece como el par de padres más probable.
     */
    public static void onVillagerChildSpawn(Villager child) {
        if (!(child.level() instanceof ServerLevel level)) return;
        if (!child.isBaby()) return;

        MinecraftServer server = level.getServer();
        if (server == null) return;
        if (SpeciesApi.hasStoredSpecies(server, child.getUUID())) return; // ya procesado

        AABB searchBox = child.getBoundingBox().inflate(PARENT_SEARCH_RADIUS);
        List<Villager> nearbyAdults = level.getEntitiesOfClass(Villager.class, searchBox,
                v -> !v.isBaby() && v.getUUID() != child.getUUID());

        if (nearbyAdults.size() < 2) {
            // No se encontraron dos padres candidatos: tratarlo como aldeano salvaje.
            onWildVillagerSpawn(child);
            return;
        }

        nearbyAdults.sort(Comparator.comparingDouble(v -> v.distanceToSqr(child)));
        onVillagerChildBorn(child, nearbyAdults.get(0), nearbyAdults.get(1));
    }

    /** Cría de dos aldeanos con padres conocidos: mezcla la herencia genética de ambos al 50/50. */
    public static void onVillagerChildBorn(Villager child, Villager parentA, Villager parentB) {
        if (!(child.level() instanceof ServerLevel level)) return;
        MinecraftServer server = level.getServer();
        if (server == null) return;

        Map<ResourceLocation, Double> heritageA = collectHeritage(server, parentA.getUUID());
        Map<ResourceLocation, Double> heritageB = collectHeritage(server, parentB.getUUID());

        Species speciesA = SpeciesApi.getSpecies(server, parentA.getUUID());
        Species speciesB = SpeciesApi.getSpecies(server, parentB.getUUID());

        UUID childUuid = child.getUUID();

        // Ambos padres son de la MISMA species pura (sin bloodlines mezclados) -> la cría también es pura.
        boolean bothPureSameSpecies = speciesA.id().equals(speciesB.id())
                && !speciesA.isHuman()
                && heritageA.size() <= 1
                && heritageB.size() <= 1;

        if (bothPureSameSpecies) {
            SpeciesApi.setSpecies(server, childUuid, speciesA.id());
            return;
        }

        Map<ResourceLocation, Double> merged = new LinkedHashMap<>();
        Set<ResourceLocation> allIds = new LinkedHashSet<>();
        allIds.addAll(heritageA.keySet());
        allIds.addAll(heritageB.keySet());
        for (ResourceLocation id : allIds) {
            double a = heritageA.getOrDefault(id, 0.0);
            double b = heritageB.getOrDefault(id, 0.0);
            merged.put(id, (a + b) / 2.0);
        }

        SpeciesApi.setSpecies(server, childUuid, SpeciesRegistry.HUMAN_ID);

        for (Map.Entry<ResourceLocation, Double> entry : merged.entrySet()) {
            if (entry.getValue() <= 0) continue;

            Optional<Bloodline> definition = BloodlineApi.getBloodlineDefinition(entry.getKey());
            if (definition.isEmpty() || !definition.get().heritable()) continue; // sin gen gemelo o no heredable
            if (!definition.get().isCompatibleWith(SpeciesRegistry.HUMAN)) continue;

            BloodlineApi.addBloodline(server, childUuid, entry.getKey(), entry.getValue());
        }
    }

    /** Al morir un aldeano no se resetea (eso es solo para jugadores): se purgan sus datos directamente. */
    public static void onVillagerDeath(Villager villager) {
        if (!(villager.level() instanceof ServerLevel level)) return;
        MinecraftServer server = level.getServer();
        if (server == null) return;

        BloodlineApi.resetBloodlines(server, villager.getUUID());
    }

    /** Una species pura no-humana cuenta como "100% sangre de esa species" a efectos de mezcla. */
    private static Map<ResourceLocation, Double> collectHeritage(MinecraftServer server, UUID uuid) {
        Map<ResourceLocation, Double> heritage = new LinkedHashMap<>();
        for (BloodlineInstance instance : BloodlineApi.getBloodlines(server, uuid)) {
            heritage.put(instance.bloodline().id(), instance.percentage());
        }
        Species species = SpeciesApi.getSpecies(server, uuid);
        if (!species.isHuman()) {
            heritage.merge(species.id(), 100.0, Double::sum);
        }
        return heritage;
    }
}
