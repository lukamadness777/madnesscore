package dev.lukamadness.madnesscore.fabric.client.dimension;

import dev.lukamadness.madnesscore.common.client.dimension.ClientDimensionSync;
import dev.lukamadness.madnesscore.common.client.tailoring.TailoringClientNetworking;
import dev.lukamadness.madnesscore.common.content.tailoring.network.TailoringCandidatesPacket;
import dev.lukamadness.madnesscore.common.network.UpdateDimensionsPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientPacketHandlers
{
    private ClientPacketHandlers() {}

    public static void register()
    {
        ClientPlayNetworking.registerGlobalReceiver(UpdateDimensionsPacket.TYPE,
                (packet, context) -> ClientDimensionSync.applyUpdateDimensions(packet));

        ClientPlayNetworking.registerGlobalReceiver(TailoringCandidatesPacket.TYPE,
                (packet, context) -> TailoringClientNetworking.applyCandidates(packet));
    }
}
