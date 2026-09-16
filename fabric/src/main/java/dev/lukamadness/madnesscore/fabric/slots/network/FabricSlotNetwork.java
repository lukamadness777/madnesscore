package dev.lukamadness.madnesscore.fabric.slots.network;

import dev.lukamadness.madnesscore.common.platform.services.ISlotNetwork;
import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotDefinitionsPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class FabricSlotNetwork implements ISlotNetwork {
    @Override
    public void sendToPlayer(ServerPlayer player, SyncSlotComponentPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToTracking(LivingEntity entity, SyncSlotComponentPayload payload) {
        for (ServerPlayer tracker : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(tracker, payload);
        }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SlotBreakPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToTracking(LivingEntity entity, SlotBreakPayload payload) {
        for (ServerPlayer tracker : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(tracker, payload);
        }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SyncSlotDefinitionsPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
