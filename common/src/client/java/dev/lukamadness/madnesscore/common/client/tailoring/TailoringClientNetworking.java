package dev.lukamadness.madnesscore.common.client.tailoring;

import dev.lukamadness.madnesscore.common.content.tailoring.network.TailoringCandidatesPacket;
import dev.lukamadness.madnesscore.common.content.tailoring.screen.TailoringTableScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class TailoringClientNetworking {
    private TailoringClientNetworking() {
    }

    public static void applyCandidates(TailoringCandidatesPacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.containerMenu.containerId == packet.containerId()
                && player.containerMenu instanceof TailoringTableScreenHandler handler) {
            handler.setCandidatesFromNetwork(packet.results());
        }
    }
}
