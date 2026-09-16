package dev.lukamadness.madnesscore.common.api.family;

import dev.lukamadness.madnesscore.common.api.bloodline.BloodlineApi;
import dev.lukamadness.madnesscore.common.api.species.Species;
import dev.lukamadness.madnesscore.common.api.species.SpeciesApi;
import dev.lukamadness.madnesscore.common.data.family.PlayerFamilyData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class FamilyApi {
    private FamilyApi() {}

    public static FamilyBuilder familyBuilder(ResourceLocation id) {
        return FamilyBuilder.create(id);
    }

    public static Family createFamily(ResourceLocation id, Component displayName, FamilyCompatibility compatibility) {
        return familyBuilder(id).displayName(displayName).compatibility(compatibility).register();
    }

    public static boolean familyExists(ResourceLocation id) {
        return FamilyRegistry.exists(id);
    }

    public static Optional<Family> getFamilyDefinition(ResourceLocation id) {
        return Optional.ofNullable(FamilyRegistry.get(id));
    }

    public static Map<ResourceLocation, Family> getAllFamilies() {
        return FamilyRegistry.getAll();
    }

    public static void registerGroup(ResourceLocation groupId, int maxFamilies) {
        FamilyRegistry.registerGroupMax(groupId, maxFamilies);
    }

    public static void registerExclusiveGroup(ResourceLocation groupId) {
        registerGroup(groupId, 1);
    }

    public static int getGroupMaxFamilies(ResourceLocation groupId) {
        return FamilyRegistry.getGroupMaxFamilies(groupId);
    }

    public static void setExclusiveWith(ResourceLocation idA, ResourceLocation idB) {
        requireFamily(idA);
        requireFamily(idB);
        FamilyRegistry.addExclusive(idA, idB);
    }

    public static boolean areExclusiveWith(ResourceLocation idA, ResourceLocation idB) {
        return FamilyRegistry.areExclusive(idA, idB);
    }

    public static boolean isFamily(MinecraftServer server, UUID uuid, ResourceLocation familyId) {
        return PlayerFamilyData.get(server).has(uuid, familyId);
    }

    public static boolean isFamily(ServerPlayer player, ResourceLocation familyId) {
        return isFamily(player.getServer(), player.getUUID(), familyId);
    }

    public static boolean isFamilyEnabled(MinecraftServer server, UUID uuid, ResourceLocation familyId) {
        return getFamily(server, uuid, familyId).map(instance -> !instance.disabled()).orElse(false);
    }

    public static Optional<FamilyInstance> getFamily(MinecraftServer server, UUID uuid, ResourceLocation familyId) {
        PlayerFamilyData data = PlayerFamilyData.get(server);
        if (!data.has(uuid, familyId)) {
            return Optional.empty();
        }
        Family family = FamilyRegistry.get(familyId);
        if (family == null) {
            return Optional.empty();
        }
        return Optional.of(new FamilyInstance(family, data.isDisabled(uuid, familyId)));
    }

    public static Optional<FamilyInstance> getFamily(ServerPlayer player, ResourceLocation familyId) {
        return getFamily(player.getServer(), player.getUUID(), familyId);
    }

    public static Set<ResourceLocation> getFamilyIds(MinecraftServer server, UUID uuid) {
        return PlayerFamilyData.get(server).getAll(uuid).keySet();
    }

    public static List<FamilyInstance> getFamilies(MinecraftServer server, UUID uuid) {
        List<FamilyInstance> result = new ArrayList<>();
        PlayerFamilyData.get(server).getAll(uuid).forEach((id, disabled) -> {
            Family family = FamilyRegistry.get(id);
            if (family != null) {
                result.add(new FamilyInstance(family, disabled));
            }
        });
        return result;
    }

    public static List<FamilyInstance> getFamilies(ServerPlayer player) {
        return getFamilies(player.getServer(), player.getUUID());
    }

    public static Optional<FamilyInstance> getFamilyGroup(MinecraftServer server, UUID uuid, ResourceLocation groupId) {
        for (FamilyInstance instance : getFamilies(server, uuid)) {
            if (groupId.equals(instance.family().group())) {
                return Optional.of(instance);
            }
        }
        return Optional.empty();
    }

    public static boolean hasFamilyAttribute(MinecraftServer server, UUID uuid, String attribute) {
        for (FamilyInstance instance : getFamilies(server, uuid)) {
            if (!instance.disabled() && instance.family().hasAttribute(attribute)) {
                return true;
            }
        }
        return false;
    }

    public static boolean addFamily(MinecraftServer server, UUID uuid, ResourceLocation familyId) {
        Family family = requireFamily(familyId);

        Species species = SpeciesApi.getSpecies(server, uuid);
        if (!family.isCompatibleWith(species)) {
            return false;
        }

        if (family.requiredBloodline().isPresent()
                && !BloodlineApi.isBloodline(server, uuid, family.requiredBloodline().get())) {
            return false;
        }

        PlayerFamilyData data = PlayerFamilyData.get(server);
        Set<ResourceLocation> current = data.getAll(uuid).keySet();

        for (ResourceLocation existing : current) {
            if (!existing.equals(familyId) && FamilyRegistry.areExclusive(familyId, existing)) {
                return false;
            }
        }

        if (family.hasGroup()) {
            int max = FamilyRegistry.getGroupMaxFamilies(family.group());
            long inGroup = current.stream()
                    .filter(id -> !id.equals(familyId))
                    .map(FamilyRegistry::get)
                    .filter(Objects::nonNull)
                    .filter(other -> family.group().equals(other.group()))
                    .count();
            if (inGroup >= max) {
                return false;
            }
        }

        boolean isNew = !data.has(uuid, familyId);
        data.set(uuid, familyId, false);

        if (isNew) {
            for (FamilyChangeListener listener : FamilyRegistry.listeners()) {
                listener.onFamilyAdded(server, uuid, family);
            }
        }
        return true;
    }

    public static boolean addFamily(ServerPlayer player, ResourceLocation familyId) {
        return addFamily(player.getServer(), player.getUUID(), familyId);
    }

    public static boolean setFamilyDisabled(MinecraftServer server, UUID uuid, ResourceLocation familyId, boolean disabled) {
        PlayerFamilyData data = PlayerFamilyData.get(server);
        if (!data.has(uuid, familyId)) {
            return false;
        }
        if (data.isDisabled(uuid, familyId) == disabled) {
            return true;
        }
        data.set(uuid, familyId, disabled);

        Family family = FamilyRegistry.get(familyId);
        if (family != null) {
            for (FamilyChangeListener listener : FamilyRegistry.listeners()) {
                if (disabled) {
                    listener.onFamilyDisabled(server, uuid, family);
                } else {
                    listener.onFamilyEnabled(server, uuid, family);
                }
            }
        }
        return true;
    }

    public static void removeFamily(MinecraftServer server, UUID uuid, ResourceLocation familyId) {
        PlayerFamilyData data = PlayerFamilyData.get(server);
        if (!data.has(uuid, familyId)) {
            return;
        }
        Family family = FamilyRegistry.get(familyId);
        data.remove(uuid, familyId);

        if (family != null) {
            for (FamilyChangeListener listener : FamilyRegistry.listeners()) {
                listener.onFamilyRemoved(server, uuid, family);
            }
        }
    }

    public static void removeFamily(ServerPlayer player, ResourceLocation familyId) {
        removeFamily(player.getServer(), player.getUUID(), familyId);
    }

    public static void resetFamilies(MinecraftServer server, UUID uuid) {
        for (ResourceLocation id : Set.copyOf(PlayerFamilyData.get(server).getAll(uuid).keySet())) {
            removeFamily(server, uuid, id);
        }
    }

    public static void resetFamilies(ServerPlayer player) {
        resetFamilies(player.getServer(), player.getUUID());
    }

    public static void onFamilyChange(FamilyChangeListener listener) {
        FamilyRegistry.addListener(Objects.requireNonNull(listener));
    }

    private static Family requireFamily(ResourceLocation id) {
        Family family = FamilyRegistry.get(id);
        if (family == null) {
            throw new IllegalArgumentException("Family unknown: " + id);
        }
        return family;
    }
}
