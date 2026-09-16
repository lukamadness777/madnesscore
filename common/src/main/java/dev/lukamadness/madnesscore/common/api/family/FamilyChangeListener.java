package dev.lukamadness.madnesscore.common.api.family;

import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public interface FamilyChangeListener {
    default void onFamilyAdded(MinecraftServer server, UUID playerUuid, Family family) {
    }

    default void onFamilyRemoved(MinecraftServer server, UUID playerUuid, Family family) {
    }

    default void onFamilyDisabled(MinecraftServer server, UUID playerUuid, Family family) {
    }

    default void onFamilyEnabled(MinecraftServer server, UUID playerUuid, Family family) {
    }
}
