package dev.lukamadness.madnesscore.common.data.species;

import dev.lukamadness.madnesscore.common.api.species.SpeciesRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSpeciesData extends SavedData {
    private static final String ID = "madnesscore_species";
    private static final String NBT_ROOT = "players";

    private final Map<UUID, ResourceLocation> speciesByPlayer = new HashMap<>();

    public boolean has(UUID uuid) {
        return speciesByPlayer.containsKey(uuid);
    }

    public ResourceLocation getSpeciesId(UUID uuid) {
        return speciesByPlayer.getOrDefault(uuid, SpeciesRegistry.HUMAN_ID);
    }

    public void setSpeciesId(UUID uuid, ResourceLocation speciesId) {
        speciesByPlayer.put(uuid, speciesId);
        setDirty();
    }

    public Map<UUID, ResourceLocation> getAll() {
        return Collections.unmodifiableMap(speciesByPlayer);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();
        speciesByPlayer.forEach((uuid, speciesId) -> players.putString(uuid.toString(), speciesId.toString()));
        tag.put(NBT_ROOT, players);
        return tag;
    }

    private static PlayerSpeciesData load(CompoundTag tag, HolderLookup.Provider registries) {
        PlayerSpeciesData data = new PlayerSpeciesData();
        CompoundTag players = tag.getCompound(NBT_ROOT);
        for (String key : players.getAllKeys()) {
            ResourceLocation speciesId = ResourceLocation.tryParse(players.getString(key));
            if (speciesId == null) {
                speciesId = SpeciesRegistry.HUMAN_ID;
            }
            data.speciesByPlayer.put(UUID.fromString(key), speciesId);
        }
        return data;
    }

    private static final Factory<PlayerSpeciesData> FACTORY =
            new Factory<>(PlayerSpeciesData::new, PlayerSpeciesData::load, null);

    public static PlayerSpeciesData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(FACTORY, ID);
    }
}
