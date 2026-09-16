package dev.lukamadness.madnesscore.neoforge.client.dimension;

import dev.lukamadness.madnesscore.common.client.dimension.ClientDimensionSync;
import dev.lukamadness.madnesscore.common.network.UpdateDimensionsPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPacketHandlers
{
    private ClientPacketHandlers() {}

    public static void handleUpdateDimensions(UpdateDimensionsPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> ClientDimensionSync.applyUpdateDimensions(packet));
    }
}
