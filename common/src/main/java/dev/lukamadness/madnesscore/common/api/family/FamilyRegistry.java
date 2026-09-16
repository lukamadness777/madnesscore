package dev.lukamadness.madnesscore.common.api.family;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class FamilyRegistry {
    private FamilyRegistry() {}

    private static final Map<ResourceLocation, Family> FAMILIES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Integer> GROUP_MAX_FAMILIES = new HashMap<>();
    private static final Map<ResourceLocation, Set<ResourceLocation>> EXCLUSIVE = new HashMap<>();
    private static final List<FamilyChangeListener> LISTENERS = new ArrayList<>();

    static Family register(ResourceLocation id, Component displayName, FamilyCompatibility compatibility,
                            ResourceLocation group, Optional<ResourceLocation> requiredBloodline,
                            Set<String> attributes, FamilyRequirementReaction onRequirementLost) {
        if (FAMILIES.containsKey(id)) {
            throw new IllegalStateException("Family already register: " + id);
        }
        Family family = new Family(id, displayName, compatibility, group, requiredBloodline,
                attributes, onRequirementLost);
        FAMILIES.put(id, family);
        return family;
    }

    static void addListener(FamilyChangeListener listener) {
        LISTENERS.add(listener);
    }

    static List<FamilyChangeListener> listeners() {
        return Collections.unmodifiableList(LISTENERS);
    }

    public static boolean exists(ResourceLocation id) {
        return FAMILIES.containsKey(id);
    }

    public static Family get(ResourceLocation id) {
        return FAMILIES.get(id);
    }

    public static Map<ResourceLocation, Family> getAll() {
        return Collections.unmodifiableMap(FAMILIES);
    }

    static void registerGroupMax(ResourceLocation groupId, int maxFamilies) {
        GROUP_MAX_FAMILIES.put(groupId, maxFamilies);
    }

    public static int getGroupMaxFamilies(ResourceLocation groupId) {
        return GROUP_MAX_FAMILIES.getOrDefault(groupId, Integer.MAX_VALUE);
    }

    static void addExclusive(ResourceLocation idA, ResourceLocation idB) {
        EXCLUSIVE.computeIfAbsent(idA, k -> new HashSet<>()).add(idB);
        EXCLUSIVE.computeIfAbsent(idB, k -> new HashSet<>()).add(idA);
    }

    public static boolean areExclusive(ResourceLocation idA, ResourceLocation idB) {
        return EXCLUSIVE.getOrDefault(idA, Set.of()).contains(idB);
    }
}
