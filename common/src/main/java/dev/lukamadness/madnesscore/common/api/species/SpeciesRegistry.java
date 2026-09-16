package dev.lukamadness.madnesscore.common.api.species;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpeciesRegistry {
    private SpeciesRegistry() {}

    private static final Map<ResourceLocation, Species> SPECIES = new LinkedHashMap<>();
    private static final List<SpeciesChangeListener> LISTENERS = new ArrayList<>();

    public static final ResourceLocation HUMAN_ID =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "human");
    public static final Species HUMAN =
            register(HUMAN_ID, Component.translatable("species.madnesscore.human"), true, 1.0);

    static Species register(ResourceLocation id, Component displayName, boolean isHumanLike, double weight) {
        if (SPECIES.containsKey(id)) {
            throw new IllegalStateException("Species ya registrada: " + id);
        }
        Species species = new Species(id, displayName, isHumanLike, weight);
        SPECIES.put(id, species);
        return species;
    }

    static void addListener(SpeciesChangeListener listener) {
        LISTENERS.add(listener);
    }

    static List<SpeciesChangeListener> listeners() {
        return Collections.unmodifiableList(LISTENERS);
    }

    public static boolean exists(ResourceLocation id) {
        return SPECIES.containsKey(id);
    }

    public static Species get(ResourceLocation id) {
        return SPECIES.get(id);
    }

    public static Species getOrHuman(ResourceLocation id) {
        return SPECIES.getOrDefault(id, HUMAN);
    }

    public static Map<ResourceLocation, Species> getAll() {
        return Collections.unmodifiableMap(SPECIES);
    }

    public static ResourceLocation rollAny() {
        return rollAnyFiltered(species -> true);
    }

    /**
     * Igual que {@link #rollAny()} pero solo considera species que cumplan el filtro dado.
     * Usado, por ejemplo, para que los aldeanos solo puedan sortear species con isHumanLike = true.
     */
    public static ResourceLocation rollAnyFiltered(java.util.function.Predicate<Species> filter) {
        List<Species> candidates = new ArrayList<>();
        double totalWeight = 0;
        for (Species s : SPECIES.values()) {
            if (s.weight() > 0 && filter.test(s)) {
                candidates.add(s);
                totalWeight += s.weight();
            }
        }
        if (candidates.isEmpty() || totalWeight <= 0) {
            return HUMAN_ID;
        }
        double roll = Math.random() * totalWeight;
        double cumulative = 0;
        for (Species s : candidates) {
            cumulative += s.weight();
            if (roll < cumulative) {
                return s.id();
            }
        }
        return candidates.get(candidates.size() - 1).id();
    }
}
