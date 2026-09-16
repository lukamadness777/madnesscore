package dev.lukamadness.madnesscore.common.event.species;

import dev.lukamadness.madnesscore.common.api.species.Species;
import dev.lukamadness.madnesscore.common.api.species.SpeciesApi;
import dev.lukamadness.madnesscore.common.registry.gamerule.ModGameRules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class SpeciesDeathHandler {
    private SpeciesDeathHandler() {}

    public static void onPlayerDeath(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        boolean resetOnDeath = player.serverLevel().getGameRules().getBoolean(ModGameRules.RESET_SPECIES_ON_DEATH);
        if (!resetOnDeath) return;

        UUID uuid = player.getUUID();
        Species oldSpecies = SpeciesApi.getSpecies(server, uuid);
        Species newSpecies = SpeciesApi.rerollSpecies(server, uuid);

        if (!newSpecies.id().equals(oldSpecies.id())) {
            player.sendSystemMessage(Component.translatable("madnesscore.species.reborn_as", newSpecies.displayName()));
        }
    }
}
