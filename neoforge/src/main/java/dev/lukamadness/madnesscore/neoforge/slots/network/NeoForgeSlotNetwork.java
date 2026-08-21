package dev.lukamadness.madnesscore.neoforge.slots.network;

import dev.lukamadness.madnesscore.common.platform.services.ISlotNetwork;
import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Implementacion NeoForge de {@link ISlotNetwork} (Fase 4) usando {@link PacketDistributor}.
 * A diferencia del equivalente Fabric, esta clase es 100% segura en ambos lados sin ningun
 * truco: {@code PacketDistributor} en si no referencia nada cliente-only al cargarse, y sus
 * metodos de envio (server -> tracking/player, client -> server) simplemente no hacen nada util
 * si se invocan del lado equivocado, sin romper la carga de clases. Los payloads y su registro
 * (canal) viven en {@link SlotNetworkRegistration}.
 */
public class NeoForgeSlotNetwork implements ISlotNetwork {

    @Override
    public void sendToPlayer(ServerPlayer player, SyncSlotComponentPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToTracking(LivingEntity entity, SyncSlotComponentPayload payload) {
        // Sin "AndSelf": el contrato de ISlotNetwork#sendToTracking excluye a la propia entidad
        // (ver el default sendToTrackingAndSelf, que ya se encarga de mandarselo aparte si hace falta).
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
}
