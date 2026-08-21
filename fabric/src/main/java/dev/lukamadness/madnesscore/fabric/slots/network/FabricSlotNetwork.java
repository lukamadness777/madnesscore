package dev.lukamadness.madnesscore.fabric.slots.network;

import dev.lukamadness.madnesscore.common.platform.services.ISlotNetwork;
import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Implementacion Fabric de {@link ISlotNetwork} (Fase 4), cargada via ServiceLoader (ver
 * {@code META-INF/services}). Vive en el sourceSet {@code main}, por lo que esta clase se carga
 * tanto en el cliente fisico como en el servidor dedicado: {@link #sendToPlayer} y
 * {@link #sendToTracking} son 100% server-side (Fabric Networking API los expone igual en
 * servidor dedicado e integrado), asi que no hay problema.
 * <p>
 */
public class FabricSlotNetwork implements ISlotNetwork {

    @Override
    public void sendToPlayer(ServerPlayer player, SyncSlotComponentPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToTracking(LivingEntity entity, SyncSlotComponentPayload payload) {
        // PlayerLookup.tracking ya excluye a la propia entidad si es un jugador (ver
        // ISlotNetwork#sendToTracking), asi que coincide exactamente con el contrato pedido.
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
}
