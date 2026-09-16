package dev.lukamadness.madnesscore.neoforge.slots.network;

import dev.lukamadness.madnesscore.common.platform.services.ISlotNetwork;
import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotDefinitionsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeSlotNetwork implements ISlotNetwork {
    @Override
    public void sendToPlayer(ServerPlayer player, SyncSlotComponentPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToTracking(LivingEntity entity, SyncSlotComponentPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SlotBreakPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToTracking(LivingEntity entity, SlotBreakPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SyncSlotDefinitionsPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
