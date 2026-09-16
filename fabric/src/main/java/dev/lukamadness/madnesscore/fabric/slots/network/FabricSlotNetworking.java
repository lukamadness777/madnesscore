package dev.lukamadness.madnesscore.fabric.slots.network;

import dev.lukamadness.madnesscore.common.slots.SlotRespawnHandler;
import dev.lukamadness.madnesscore.common.slots.network.RequestSlotResyncPayload;
import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotDefinitionsPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FabricSlotNetworking {
    private FabricSlotNetworking() {
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(SyncSlotComponentPayload.TYPE, SyncSlotComponentPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncSlotDefinitionsPayload.TYPE, SyncSlotDefinitionsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SlotBreakPayload.TYPE, SlotBreakPayload.STREAM_CODEC);

        PayloadTypeRegistry.playC2S().register(RequestSlotResyncPayload.TYPE, RequestSlotResyncPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestSlotResyncPayload.TYPE, (payload, context) ->
                SlotRespawnHandler.handleClientResyncRequest(context.player()));
    }
}