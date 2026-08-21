package dev.lukamadness.madnesscore.common.platform.services;

import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Servicio de plataforma (Fase 4) que resuelve como se envian los paquetes de red del sistema de
 * slots. El codigo common no sabe (ni le importa) si por debajo se usa la Networking API de
 * Fabric o el {@code PacketDistributor} de NeoForge; los payloads en si (records vanilla
 * {@code CustomPacketPayload}, ver paquete {@code slots.network}) son 100% comunes, solo el envio
 * y el registro del canal varian por loader.
 */
public interface ISlotNetwork {

    /**
     * Envia un paquete de sincronizacion a un jugador puntual (ej: al empezar a trackear una
     * entidad, o al loguearse para verse a si mismo).
     */
    void sendToPlayer(ServerPlayer player, SyncSlotComponentPayload payload);

    /**
     * Envia un paquete de sincronizacion a todos los jugadores que actualmente trackean la
     * entidad (sin incluirla a ella misma aunque sea un jugador).
     */
    void sendToTracking(LivingEntity entity, SyncSlotComponentPayload payload);

    /**
     * Conveniencia: envia el paquete a todos los que trackean la entidad y, si la entidad misma
     * es un {@link ServerPlayer}, tambien a ella (para que actualice su propio modelo en camara
     * en tercera persona / a otros sistemas que dependan del componente sincronizado).
     */
    default void sendToTrackingAndSelf(LivingEntity entity, SyncSlotComponentPayload payload) {
        sendToTracking(entity, payload);
        if (entity instanceof ServerPlayer player) {
            sendToPlayer(player, payload);
        }
    }

    /**
     * Igual que {@link #sendToPlayer(ServerPlayer, SyncSlotComponentPayload)} pero para el
     * paquete "fire and forget" de rotura de item ({@link SlotBreakPayload}). Portado de
     * {@code TrinketsApi#onTrinketBroken}.
     */
    void sendToPlayer(ServerPlayer player, SlotBreakPayload payload);

    /**
     * Igual que {@link #sendToTracking(LivingEntity, SyncSlotComponentPayload)} pero para
     * {@link SlotBreakPayload}.
     */
    void sendToTracking(LivingEntity entity, SlotBreakPayload payload);

    default void sendToTrackingAndSelf(LivingEntity entity, SlotBreakPayload payload) {
        sendToTracking(entity, payload);
        if (entity instanceof ServerPlayer player) {
            sendToPlayer(player, payload);
        }
    }
}