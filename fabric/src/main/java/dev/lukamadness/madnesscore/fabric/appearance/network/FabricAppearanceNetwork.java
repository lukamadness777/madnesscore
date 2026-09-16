package dev.lukamadness.madnesscore.fabric.appearance.network;

import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;
import dev.lukamadness.madnesscore.common.platform.services.IAppearanceNetwork;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class FabricAppearanceNetwork implements IAppearanceNetwork {
    private static volatile Consumer<AppearanceConfigPayload> clientSender;

    public static void setClientSender(Consumer<AppearanceConfigPayload> sender) {
        clientSender = sender;
    }

    @Override
    public void sendToServer(AppearanceConfigPayload payload) {
        Consumer<AppearanceConfigPayload> sender = clientSender;
        if (sender != null) {
            sender.accept(payload);
        }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, AppearanceConfigPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
