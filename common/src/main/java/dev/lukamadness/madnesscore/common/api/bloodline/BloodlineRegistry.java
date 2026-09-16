package dev.lukamadness.madnesscore.common.api.bloodline;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BloodlineRegistry {
    private BloodlineRegistry() {}

    private static final Map<ResourceLocation, Bloodline> BLOODLINES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Set<ResourceLocation>> MUTUALLY_EXCLUSIVE = new HashMap<>();
    private static final List<BloodlineChangeListener> LISTENERS = new ArrayList<>();

    static Bloodline register(ResourceLocation id, Component displayName, BloodlineCompatibility compatibility,
                               Set<String> attributes, boolean natural, boolean obtainable, boolean heritable,
                               boolean exclusiveObtainMethod) {
        if (BLOODLINES.containsKey(id)) {
            throw new IllegalStateException("Bloodline ya registrada: " + id);
        }
        Bloodline bloodline = new Bloodline(id, displayName, compatibility, attributes,
                natural, obtainable, heritable, exclusiveObtainMethod);
        BLOODLINES.put(id, bloodline);
        return bloodline;
    }

    static void addListener(BloodlineChangeListener listener) {
        LISTENERS.add(listener);
    }

    static List<BloodlineChangeListener> listeners() {
        return Collections.unmodifiableList(LISTENERS);
    }

    public static boolean exists(ResourceLocation id) {
        return BLOODLINES.containsKey(id);
    }

    public static Bloodline get(ResourceLocation id) {
        return BLOODLINES.get(id);
    }

    public static Map<ResourceLocation, Bloodline> getAll() {
        return Collections.unmodifiableMap(BLOODLINES);
    }

    static void addMutualExclusivity(ResourceLocation idA, ResourceLocation idB) {
        MUTUALLY_EXCLUSIVE.computeIfAbsent(idA, k -> new HashSet<>()).add(idB);
        MUTUALLY_EXCLUSIVE.computeIfAbsent(idB, k -> new HashSet<>()).add(idA);
    }

    public static boolean areMutuallyExclusive(ResourceLocation idA, ResourceLocation idB) {
        return MUTUALLY_EXCLUSIVE.getOrDefault(idA, Set.of()).contains(idB);
    }
}
