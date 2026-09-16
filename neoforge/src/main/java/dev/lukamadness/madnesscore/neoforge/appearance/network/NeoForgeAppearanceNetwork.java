package dev.lukamadness.madnesscore.neoforge.appearance.network;

import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;
import dev.lukamadness.madnesscore.common.platform.services.IAppearanceNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeAppearanceNetwork implements IAppearanceNetwork {
    @Override
    public void sendToServer(AppearanceConfigPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, AppearanceConfigPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
