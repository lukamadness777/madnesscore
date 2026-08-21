package dev.lukamadness.madnesscore.fabric.slots.network;

import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotDefinitionsPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Registro de canal (Fase 4) para Fabric: da de alta los payloads de sincronizacion en el
 * {@link PayloadTypeRegistry} (obligatorio para que Fabric sepa serializarlo/enrutarlo). El
 * receiver (S2C) vive del lado cliente (ver {@code FabricClientSlotNetworking}).
 */
public final class FabricSlotNetworking {

    private FabricSlotNetworking() {
    }

    /**
     * Llamar UNA vez desde {@code MadnessCoreFabric#onInitialize} (main). Corre tanto en cliente
     * integrado como en servidor dedicado; el registro de tipos es seguro en ambos.
     */
    public static void init() {
        PayloadTypeRegistry.playS2C().register(SyncSlotComponentPayload.TYPE, SyncSlotComponentPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncSlotDefinitionsPayload.TYPE, SyncSlotDefinitionsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SlotBreakPayload.TYPE, SlotBreakPayload.STREAM_CODEC);
    }
}