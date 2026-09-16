package dev.lukamadness.madnesscore.common.api.species;

import dev.lukamadness.madnesscore.common.data.species.PlayerSpeciesData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class SpeciesApi {
    private SpeciesApi() {}

    public static Species createSpecies(ResourceLocation id, Component displayName, boolean isHumanLike, double weight) {
        return SpeciesRegistry.register(id, displayName, isHumanLike, weight);
    }

    public static SpeciesBuilder speciesBuilder(ResourceLocation id) {
        return SpeciesBuilder.create(id);
    }

    public static boolean speciesExists(ResourceLocation id) {
        return SpeciesRegistry.exists(id);
    }

    public static Species getSpeciesDefinition(ResourceLocation id) {
        return SpeciesRegistry.getOrHuman(id);
    }

    public static Map<ResourceLocation, Species> getAllSpecies() {
        return SpeciesRegistry.getAll();
    }

    public static Species human() {
        return SpeciesRegistry.HUMAN;
    }

    public static Species getSpecies(MinecraftServer server, UUID uuid) {
        ResourceLocation id = PlayerSpeciesData.get(server).getSpeciesId(uuid);
        return SpeciesRegistry.getOrHuman(id);
    }

    public static Species getSpecies(ServerPlayer player) {
        return getSpecies(player.getServer(), player.getUUID());
    }

    public static boolean isSpecies(MinecraftServer server, UUID uuid, ResourceLocation speciesId) {
        return getSpecies(server, uuid).id().equals(speciesId);
    }

    public static boolean isSpecies(ServerPlayer player, ResourceLocation speciesId) {
        return isSpecies(player.getServer(), player.getUUID(), speciesId);
    }

    public static boolean isHumanLike(MinecraftServer server, UUID uuid) {
        return getSpecies(server, uuid).isHumanLike();
    }

    public static boolean isHumanLike(ServerPlayer player) {
        return isHumanLike(player.getServer(), player.getUUID());
    }

    public static boolean hasStoredSpecies(MinecraftServer server, UUID uuid) {
        return PlayerSpeciesData.get(server).has(uuid);
    }

    public static void setSpecies(MinecraftServer server, UUID uuid, ResourceLocation speciesId) {
        if (!SpeciesRegistry.exists(speciesId)) {
            throw new IllegalArgumentException("Species unknown: " + speciesId);
        }
        Species oldSpecies = getSpecies(server, uuid);
        Species newSpecies = SpeciesRegistry.get(speciesId);
        if (oldSpecies.id().equals(newSpecies.id())) {
            return;
        }

        PlayerSpeciesData.get(server).setSpeciesId(uuid, speciesId);

        for (SpeciesChangeListener listener : SpeciesRegistry.listeners()) {
            listener.onSpeciesChanged(server, uuid, oldSpecies, newSpecies);
        }
    }

    public static void setSpecies(ServerPlayer player, ResourceLocation speciesId) {
        setSpecies(player.getServer(), player.getUUID(), speciesId);
    }

    public static void resetSpecies(MinecraftServer server, UUID uuid) {
        setSpecies(server, uuid, SpeciesRegistry.HUMAN_ID);
    }

    public static void resetSpecies(ServerPlayer player) {
        resetSpecies(player.getServer(), player.getUUID());
    }

    public static Species rerollSpecies(MinecraftServer server, UUID uuid) {
        ResourceLocation rolled = SpeciesRegistry.rollAny();
        setSpecies(server, uuid, rolled);
        return SpeciesRegistry.get(rolled);
    }

    public static Species rerollSpecies(ServerPlayer player) {
        return rerollSpecies(player.getServer(), player.getUUID());
    }

    public static void onSpeciesChange(SpeciesChangeListener listener) {
        SpeciesRegistry.addListener(Objects.requireNonNull(listener));
    }
}
