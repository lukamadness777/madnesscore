package dev.lukamadness.madnesscore.common.api.bloodline;

import dev.lukamadness.madnesscore.common.api.species.Species;
import dev.lukamadness.madnesscore.common.api.species.SpeciesApi;
import dev.lukamadness.madnesscore.common.data.bloodline.PlayerBloodlineData;
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

public final class BloodlineApi {
    private BloodlineApi() {}

    public static BloodlineBuilder bloodlineBuilder(ResourceLocation id) {
        return BloodlineBuilder.create(id);
    }

    public static Bloodline createBloodline(ResourceLocation id, Component displayName, BloodlineCompatibility compatibility) {
        return bloodlineBuilder(id).displayName(displayName).compatibility(compatibility).register();
    }

    public static boolean bloodlineExists(ResourceLocation id) {
        return BloodlineRegistry.exists(id);
    }

    public static Optional<Bloodline> getBloodlineDefinition(ResourceLocation id) {
        return Optional.ofNullable(BloodlineRegistry.get(id));
    }

    public static Map<ResourceLocation, Bloodline> getAllBloodlines() {
        return BloodlineRegistry.getAll();
    }

    public static boolean isCompatible(ResourceLocation bloodlineId, Species species) {
        return requireBloodline(bloodlineId).isCompatibleWith(species);
    }

    public static void setMutuallyExclusive(ResourceLocation idA, ResourceLocation idB) {
        requireBloodline(idA);
        requireBloodline(idB);
        BloodlineRegistry.addMutualExclusivity(idA, idB);
    }

    public static boolean areMutuallyExclusive(ResourceLocation idA, ResourceLocation idB) {
        return BloodlineRegistry.areMutuallyExclusive(idA, idB);
    }

    public static boolean isBloodline(MinecraftServer server, UUID uuid, ResourceLocation bloodlineId) {
        return PlayerBloodlineData.get(server).has(uuid, bloodlineId);
    }

    public static boolean isBloodline(ServerPlayer player, ResourceLocation bloodlineId) {
        return isBloodline(player.getServer(), player.getUUID(), bloodlineId);
    }

    public static boolean isBloodlineOrSpecies(MinecraftServer server, UUID uuid, ResourceLocation id) {
        return isBloodline(server, uuid, id) || SpeciesApi.isSpecies(server, uuid, id);
    }

    public static double getBloodlinePercentage(MinecraftServer server, UUID uuid, ResourceLocation bloodlineId) {
        return PlayerBloodlineData.get(server).getPercentage(uuid, bloodlineId);
    }

    public static Set<ResourceLocation> getBloodlineIds(MinecraftServer server, UUID uuid) {
        return PlayerBloodlineData.get(server).getAll(uuid).keySet();
    }

    public static List<BloodlineInstance> getBloodlines(MinecraftServer server, UUID uuid) {
        List<BloodlineInstance> result = new ArrayList<>();
        PlayerBloodlineData.get(server).getAll(uuid).forEach((id, percentage) -> {
            Bloodline bloodline = BloodlineRegistry.get(id);
            if (bloodline != null) {
                result.add(new BloodlineInstance(bloodline, percentage));
            }
        });
        return result;
    }

    public static boolean hasBloodlineAttribute(MinecraftServer server, UUID uuid, String attribute) {
        for (BloodlineInstance instance : getBloodlines(server, uuid)) {
            if (instance.bloodline().hasAttribute(attribute)) {
                return true;
            }
        }
        return false;
    }

    public static boolean addBloodline(MinecraftServer server, UUID uuid, ResourceLocation bloodlineId) {
        return addBloodline(server, uuid, bloodlineId, 100.0);
    }

    public static boolean addBloodline(MinecraftServer server, UUID uuid, ResourceLocation bloodlineId, double percentage) {
        Bloodline bloodline = requireBloodline(bloodlineId);

        Species species = SpeciesApi.getSpecies(server, uuid);
        if (!bloodline.isCompatibleWith(species)) {
            return false;
        }

        PlayerBloodlineData data = PlayerBloodlineData.get(server);
        for (ResourceLocation existing : data.getAll(uuid).keySet()) {
            if (!existing.equals(bloodlineId) && BloodlineRegistry.areMutuallyExclusive(bloodlineId, existing)) {
                return false;
            }
        }

        boolean isNew = !data.has(uuid, bloodlineId);
        data.set(uuid, bloodlineId, percentage);

        if (isNew) {
            for (BloodlineChangeListener listener : BloodlineRegistry.listeners()) {
                listener.onBloodlineAdded(server, uuid, bloodline, percentage);
            }
        }
        return true;
    }

    public static boolean addBloodline(ServerPlayer player, ResourceLocation bloodlineId, double percentage) {
        return addBloodline(player.getServer(), player.getUUID(), bloodlineId, percentage);
    }

    public static boolean setBloodlinePercentage(MinecraftServer server, UUID uuid, ResourceLocation bloodlineId, double percentage) {
        PlayerBloodlineData data = PlayerBloodlineData.get(server);
        if (!data.has(uuid, bloodlineId)) {
            return false;
        }
        data.set(uuid, bloodlineId, percentage);
        return true;
    }

    public static void removeBloodline(MinecraftServer server, UUID uuid, ResourceLocation bloodlineId) {
        PlayerBloodlineData data = PlayerBloodlineData.get(server);
        if (!data.has(uuid, bloodlineId)) {
            return;
        }
        Bloodline bloodline = BloodlineRegistry.get(bloodlineId);
        data.remove(uuid, bloodlineId);

        if (bloodline != null) {
            for (BloodlineChangeListener listener : BloodlineRegistry.listeners()) {
                listener.onBloodlineRemoved(server, uuid, bloodline);
            }
        }
    }

    public static void removeBloodline(ServerPlayer player, ResourceLocation bloodlineId) {
        removeBloodline(player.getServer(), player.getUUID(), bloodlineId);
    }

    public static void resetBloodlines(MinecraftServer server, UUID uuid) {
        for (ResourceLocation id : Set.copyOf(PlayerBloodlineData.get(server).getAll(uuid).keySet())) {
            removeBloodline(server, uuid, id);
        }
    }

    public static void resetBloodlines(ServerPlayer player) {
        resetBloodlines(player.getServer(), player.getUUID());
    }

    public static void onBloodlineChange(BloodlineChangeListener listener) {
        BloodlineRegistry.addListener(Objects.requireNonNull(listener));
    }

    private static Bloodline requireBloodline(ResourceLocation id) {
        Bloodline bloodline = BloodlineRegistry.get(id);
        if (bloodline == null) {
            throw new IllegalArgumentException("Bloodline unknown: " + id);
        }
        return bloodline;
    }
}
