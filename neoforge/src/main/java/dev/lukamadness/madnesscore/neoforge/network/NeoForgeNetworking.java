package dev.lukamadness.madnesscore.neoforge.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.tailoring.TailoringClientNetworking;
import dev.lukamadness.madnesscore.common.content.tailoring.network.TailoringCandidatesPacket;
import dev.lukamadness.madnesscore.common.network.UpdateDimensionsPacket;
import dev.lukamadness.madnesscore.neoforge.client.dimension.ClientPacketHandlers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MadnessCoreCommon.MOD_ID)
public final class NeoForgeNetworking
{
    private NeoForgeNetworking() {}

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar(MadnessCoreCommon.MOD_ID);

        registrar.playToClient(UpdateDimensionsPacket.TYPE, UpdateDimensionsPacket.STREAM_CODEC,
                ClientPacketHandlers::handleUpdateDimensions);

        registrar.playToClient(TailoringCandidatesPacket.TYPE, TailoringCandidatesPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> TailoringClientNetworking.applyCandidates(payload)));
    }
}
