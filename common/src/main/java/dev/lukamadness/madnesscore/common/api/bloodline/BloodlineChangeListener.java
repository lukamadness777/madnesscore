package dev.lukamadness.madnesscore.common.api.bloodline;

import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public interface BloodlineChangeListener {
    default void onBloodlineAdded(MinecraftServer server, UUID playerUuid, Bloodline bloodline, double percentage) {
    }

    default void onBloodlineRemoved(MinecraftServer server, UUID playerUuid, Bloodline bloodline) {
    }
}
