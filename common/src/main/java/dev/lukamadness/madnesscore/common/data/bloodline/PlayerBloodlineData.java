package dev.lukamadness.madnesscore.common.data.bloodline;

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

public class PlayerBloodlineData extends SavedData {
    private static final String ID = "madnesscore_bloodlines";
    private static final String NBT_ROOT = "players";

    private final Map<UUID, Map<ResourceLocation, Double>> bloodlinesByPlayer = new HashMap<>();

    public Map<ResourceLocation, Double> getAll(UUID uuid) {
        Map<ResourceLocation, Double> stored = bloodlinesByPlayer.get(uuid);
        return stored == null ? Map.of() : Map.copyOf(stored);
    }

    public Map<UUID, Map<ResourceLocation, Double>> getAllPlayers() {
        Map<UUID, Map<ResourceLocation, Double>> copy = new LinkedHashMap<>();
        bloodlinesByPlayer.forEach((uuid, bloodlines) -> copy.put(uuid, Map.copyOf(bloodlines)));
        return Map.copyOf(copy);
    }

    public boolean has(UUID uuid, ResourceLocation bloodlineId) {
        Map<ResourceLocation, Double> stored = bloodlinesByPlayer.get(uuid);
        return stored != null && stored.containsKey(bloodlineId);
    }

    public double getPercentage(UUID uuid, ResourceLocation bloodlineId) {
        Map<ResourceLocation, Double> stored = bloodlinesByPlayer.get(uuid);
        return stored == null ? 0.0 : stored.getOrDefault(bloodlineId, 0.0);
    }

    public void set(UUID uuid, ResourceLocation bloodlineId, double percentage) {
        bloodlinesByPlayer.computeIfAbsent(uuid, k -> new HashMap<>()).put(bloodlineId, percentage);
        setDirty();
    }

    public void remove(UUID uuid, ResourceLocation bloodlineId) {
        Map<ResourceLocation, Double> stored = bloodlinesByPlayer.get(uuid);
        if (stored != null && stored.remove(bloodlineId) != null) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();
        bloodlinesByPlayer.forEach((uuid, bloodlines) -> {
            CompoundTag playerTag = new CompoundTag();
            bloodlines.forEach((id, percentage) -> playerTag.putDouble(id.toString(), percentage));
            players.put(uuid.toString(), playerTag);
        });
        tag.put(NBT_ROOT, players);
        return tag;
    }

    private static PlayerBloodlineData load(CompoundTag tag, HolderLookup.Provider registries) {
        PlayerBloodlineData data = new PlayerBloodlineData();
        CompoundTag players = tag.getCompound(NBT_ROOT);
        for (String uuidKey : players.getAllKeys()) {
            CompoundTag playerTag = players.getCompound(uuidKey);
            Map<ResourceLocation, Double> bloodlines = new HashMap<>();
            for (String bloodlineKey : playerTag.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(bloodlineKey);
                if (id != null) {
                    bloodlines.put(id, playerTag.getDouble(bloodlineKey));
                }
            }
            data.bloodlinesByPlayer.put(UUID.fromString(uuidKey), bloodlines);
        }
        return data;
    }

    private static final Factory<PlayerBloodlineData> FACTORY =
            new Factory<>(PlayerBloodlineData::new, PlayerBloodlineData::load, null);

    public static PlayerBloodlineData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(FACTORY, ID);
    }
}
