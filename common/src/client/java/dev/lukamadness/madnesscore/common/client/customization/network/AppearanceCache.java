package dev.lukamadness.madnesscore.common.client.customization.network;

import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AppearanceCache {
    private static final Map<UUID, AppearanceConfigPayload> CACHE = new ConcurrentHashMap<>();

    private AppearanceCache() {}

    public static void put(UUID uuid, AppearanceConfigPayload cfg) {
        CACHE.put(uuid, cfg);
    }

    public static AppearanceConfigPayload get(UUID uuid) {
        return CACHE.get(uuid);
    }

    public static void clear() {
        CACHE.clear();
    }
}
