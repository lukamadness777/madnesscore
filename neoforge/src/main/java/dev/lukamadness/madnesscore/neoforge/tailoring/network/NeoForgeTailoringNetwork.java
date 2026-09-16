package dev.lukamadness.madnesscore.neoforge.tailoring.network;

import dev.lukamadness.madnesscore.common.content.tailoring.network.TailoringCandidatesPacket;
import dev.lukamadness.madnesscore.common.platform.services.ITailoringNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeTailoringNetwork implements ITailoringNetwork {
    @Override
    public void sendToPlayer(ServerPlayer player, TailoringCandidatesPacket payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
