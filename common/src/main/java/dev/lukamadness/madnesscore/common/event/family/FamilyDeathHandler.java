package dev.lukamadness.madnesscore.common.event.family;

import dev.lukamadness.madnesscore.common.api.family.FamilyApi;
import dev.lukamadness.madnesscore.common.registry.gamerule.ModGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class FamilyDeathHandler {
    private FamilyDeathHandler() {}

    public static void onPlayerDeath(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        boolean resetOnDeath = player.serverLevel().getGameRules().getBoolean(ModGameRules.RESET_FAMILIES_ON_DEATH);
        if (!resetOnDeath) return;

        FamilyApi.resetFamilies(server, player.getUUID());
    }
}
