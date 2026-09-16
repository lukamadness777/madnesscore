package dev.lukamadness.madnesscore.common.platform.services;

import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;
import net.minecraft.server.level.ServerPlayer;

public interface IAppearanceNetwork {
    void sendToServer(AppearanceConfigPayload payload);

    void sendToPlayer(ServerPlayer player, AppearanceConfigPayload payload);

    default void sendToAll(ServerPlayer any, AppearanceConfigPayload payload) {
        any.getServer().getPlayerList().getPlayers().forEach(p -> sendToPlayer(p, payload));
    }
}
