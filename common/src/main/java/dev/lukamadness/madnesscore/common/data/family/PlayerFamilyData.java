package dev.lukamadness.madnesscore.common.data.family;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerFamilyData extends SavedData {
    private static final String ID = "madnesscore_families";
    private static final String NBT_ROOT = "players";

    private final Map<UUID, Map<ResourceLocation, Boolean>> familiesByPlayer = new HashMap<>();

    public Map<ResourceLocation, Boolean> getAll(UUID uuid) {
        Map<ResourceLocation, Boolean> stored = familiesByPlayer.get(uuid);
        return stored == null ? Map.of() : Map.copyOf(stored);
    }

    public Map<UUID, Map<ResourceLocation, Boolean>> getAllPlayers() {
        Map<UUID, Map<ResourceLocation, Boolean>> copy = new LinkedHashMap<>();
        familiesByPlayer.forEach((uuid, families) -> copy.put(uuid, Map.copyOf(families)));
        return Map.copyOf(copy);
    }

    public boolean has(UUID uuid, ResourceLocation familyId) {
        Map<ResourceLocation, Boolean> stored = familiesByPlayer.get(uuid);
        return stored != null && stored.containsKey(familyId);
    }

    public boolean isDisabled(UUID uuid, ResourceLocation familyId) {
        Map<ResourceLocation, Boolean> stored = familiesByPlayer.get(uuid);
        return stored != null && stored.getOrDefault(familyId, false);
    }

    public void set(UUID uuid, ResourceLocation familyId, boolean disabled) {
        familiesByPlayer.computeIfAbsent(uuid, k -> new HashMap<>()).put(familyId, disabled);
        setDirty();
    }

    public void remove(UUID uuid, ResourceLocation familyId) {
        Map<ResourceLocation, Boolean> stored = familiesByPlayer.get(uuid);
        if (stored != null && stored.remove(familyId) != null) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();
        familiesByPlayer.forEach((uuid, families) -> {
            CompoundTag playerTag = new CompoundTag();
            families.forEach((id, disabled) -> playerTag.putBoolean(id.toString(), disabled));
            players.put(uuid.toString(), playerTag);
        });
        tag.put(NBT_ROOT, players);
        return tag;
    }

    private static PlayerFamilyData load(CompoundTag tag, HolderLookup.Provider registries) {
        PlayerFamilyData data = new PlayerFamilyData();
        CompoundTag players = tag.getCompound(NBT_ROOT);
        for (String uuidKey : players.getAllKeys()) {
            CompoundTag playerTag = players.getCompound(uuidKey);
            Map<ResourceLocation, Boolean> families = new HashMap<>();
            for (String familyKey : playerTag.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(familyKey);
                if (id != null) {
                    families.put(id, playerTag.getBoolean(familyKey));
                }
            }
            data.familiesByPlayer.put(UUID.fromString(uuidKey), families);
        }
        return data;
    }

    private static final Factory<PlayerFamilyData> FACTORY =
            new Factory<>(PlayerFamilyData::new, PlayerFamilyData::load, null);

    public static PlayerFamilyData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(FACTORY, ID);
    }
}
