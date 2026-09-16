package dev.lukamadness.madnesscore.common.event.bloodline;

import dev.lukamadness.madnesscore.common.api.bloodline.BloodlineApi;
import dev.lukamadness.madnesscore.common.registry.gamerule.ModGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class BloodlineDeathHandler {
    private BloodlineDeathHandler() {}

    public static void onPlayerDeath(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        boolean resetOnDeath = player.serverLevel().getGameRules().getBoolean(ModGameRules.RESET_BLOODLINES_ON_DEATH);
        if (!resetOnDeath) return;

        BloodlineApi.resetBloodlines(server, player.getUUID());
    }
}
