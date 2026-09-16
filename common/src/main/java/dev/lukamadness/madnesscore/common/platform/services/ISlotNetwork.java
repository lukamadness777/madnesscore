package dev.lukamadness.madnesscore.common.platform.services;

import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotDefinitionsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public interface ISlotNetwork {
    void sendToPlayer(ServerPlayer player, SyncSlotComponentPayload payload);

    void sendToTracking(LivingEntity entity, SyncSlotComponentPayload payload);

    default void sendToTrackingAndSelf(LivingEntity entity, SyncSlotComponentPayload payload) {
        sendToTracking(entity, payload);
        if (entity instanceof ServerPlayer player) {
            sendToPlayer(player, payload);
        }
    }

    void sendToPlayer(ServerPlayer player, SlotBreakPayload payload);

    void sendToTracking(LivingEntity entity, SlotBreakPayload payload);

    default void sendToTrackingAndSelf(LivingEntity entity, SlotBreakPayload payload) {
        sendToTracking(entity, payload);
        if (entity instanceof ServerPlayer player) {
            sendToPlayer(player, payload);
        }
    }

    void sendToPlayer(ServerPlayer player, SyncSlotDefinitionsPayload payload);
}
