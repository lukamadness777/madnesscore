package dev.lukamadness.madnesscore.fabric.tailoring.network;

import dev.lukamadness.madnesscore.common.content.tailoring.network.TailoringCandidatesPacket;
import dev.lukamadness.madnesscore.common.platform.services.ITailoringNetwork;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class FabricTailoringNetwork implements ITailoringNetwork {
    @Override
    public void sendToPlayer(ServerPlayer player, TailoringCandidatesPacket payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
