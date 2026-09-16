package dev.lukamadness.madnesscore.common.api.species;

import net.minecraft.server.MinecraftServer;

import java.util.UUID;

@FunctionalInterface
public interface SpeciesChangeListener {
    void onSpeciesChanged(MinecraftServer server, UUID playerUuid, Species oldSpecies, Species newSpecies);
}
