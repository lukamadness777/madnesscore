package dev.lukamadness.madnesscore.common.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AppearanceStore {
    private static final Map<UUID, AppearanceConfigPayload> CONFIGS = new ConcurrentHashMap<>();

    private AppearanceStore() {}

    public static void put(UUID uuid, AppearanceConfigPayload cfg) {
        CONFIGS.put(uuid, cfg);
    }

    public static AppearanceConfigPayload get(UUID uuid) {
        return CONFIGS.get(uuid);
    }

    public static Map<UUID, AppearanceConfigPayload> getAll() {
        return CONFIGS;
    }

    public static void remove(UUID uuid) {
        CONFIGS.remove(uuid);
    }
}
