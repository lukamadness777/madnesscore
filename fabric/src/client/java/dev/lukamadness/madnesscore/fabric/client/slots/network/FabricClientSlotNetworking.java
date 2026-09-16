package dev.lukamadness.madnesscore.fabric.client.slots.network;

import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotDefinitionsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FabricClientSlotNetworking {
    private FabricClientSlotNetworking() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SyncSlotComponentPayload.TYPE, (payload, context) ->
                SlotNetworking.handleSyncOnClient(payload, context.client().level));

        ClientPlayNetworking.registerGlobalReceiver(SyncSlotDefinitionsPayload.TYPE, (payload, context) ->
                SlotNetworking.handleDefinitionsSyncOnClient(payload, context.player()));

        ClientPlayNetworking.registerGlobalReceiver(SlotBreakPayload.TYPE, (payload, context) ->
                SlotNetworking.handleBreakOnClient(payload, context.client().level));
    }
}
