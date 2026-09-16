package dev.lukamadness.madnesscore.fabric.client.appearance.network;

import dev.lukamadness.madnesscore.common.client.customization.network.AppearanceClientHandler;
import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FabricClientAppearanceNetworking {
    private FabricClientAppearanceNetworking() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(AppearanceConfigPayload.TYPE, (payload, context) ->
                AppearanceClientHandler.handleReceived(payload));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                AppearanceClientHandler.clear());
    }
}
