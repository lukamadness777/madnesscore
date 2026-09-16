package dev.lukamadness.madnesscore.common.platform.services;

import dev.lukamadness.madnesscore.common.content.tailoring.network.TailoringCandidatesPacket;
import net.minecraft.server.level.ServerPlayer;

public interface ITailoringNetwork {
    void sendToPlayer(ServerPlayer player, TailoringCandidatesPacket payload);
}
