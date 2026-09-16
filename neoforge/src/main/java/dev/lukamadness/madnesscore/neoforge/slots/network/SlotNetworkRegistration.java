package dev.lukamadness.madnesscore.neoforge.slots.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.slots.SlotRespawnHandler;
import dev.lukamadness.madnesscore.common.slots.network.RequestSlotResyncPayload;
import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotDefinitionsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MadnessCoreCommon.MOD_ID)
public final class SlotNetworkRegistration {
    private SlotNetworkRegistration() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(SyncSlotComponentPayload.TYPE, SyncSlotComponentPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> SlotNetworking.handleSyncOnClient(payload, context.player().level())));

        registrar.playToClient(SyncSlotDefinitionsPayload.TYPE, SyncSlotDefinitionsPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> SlotNetworking.handleDefinitionsSyncOnClient(payload, context.player())));

        registrar.playToClient(SlotBreakPayload.TYPE, SlotBreakPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> SlotNetworking.handleBreakOnClient(payload, context.player().level())));

        registrar.playToServer(RequestSlotResyncPayload.TYPE, RequestSlotResyncPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        SlotRespawnHandler.handleClientResyncRequest(serverPlayer);
                    }
                }));
    }
}