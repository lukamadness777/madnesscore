package dev.lukamadness.madnesscore.fabric.appearance.network;

import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;
import dev.lukamadness.madnesscore.common.network.AppearanceServerHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FabricAppearanceNetworking {
    private FabricAppearanceNetworking() {
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(AppearanceConfigPayload.TYPE, AppearanceConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(AppearanceConfigPayload.TYPE, AppearanceConfigPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(AppearanceConfigPayload.TYPE, (payload, context) ->
                AppearanceServerHandler.handleReceived(context.player(), payload));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                AppearanceServerHandler.onPlayerJoin(handler.getPlayer()));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                AppearanceServerHandler.onPlayerDisconnect(handler.getPlayer()));
    }
}
