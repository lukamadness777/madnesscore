package dev.lukamadness.madnesscore.neoforge.slots.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotDefinitionsPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registro de canal (Fase 4) para NeoForge: da de alta el payload de sincronizacion y su handler
 * en un {@link RegisterPayloadHandlersEvent}, disparado en el bus de mod.
 * {@code @EventBusSubscriber} se auto-registra (mismo patron que
 * {@code MadnessCoreNeoForgeClient}), asi que no hace falta ninguna llamada manual desde
 * {@code MadnessCoreNeoForge(IEventBus)}.
 */
@EventBusSubscriber(modid = MadnessCoreCommon.MOD_ID)
public final class SlotNetworkRegistration {

    private SlotNetworkRegistration() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(SyncSlotComponentPayload.TYPE, SyncSlotComponentPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> SlotNetworking.handleSyncOnClient(payload, context.player().level())));

        // context.player() dentro de un handler playToClient es siempre el jugador local (este
        // handler solo corre del lado cliente): se lo pasamos a handleDefinitionsSyncOnClient
        // para que pueda forzar el rebuild del InventoryMenu local (ver el javadoc del metodo).
        registrar.playToClient(SyncSlotDefinitionsPayload.TYPE, SyncSlotDefinitionsPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> SlotNetworking.handleDefinitionsSyncOnClient(payload, context.player())));

        // "Fire and forget": reproduce el efecto de rotura (sonido + particulas) de un item
        // equipado en un slot dinamico. Portado del receiver de BreakPayload en TrinketsClient.
        registrar.playToClient(SlotBreakPayload.TYPE, SlotBreakPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> SlotNetworking.handleBreakOnClient(payload, context.player().level())));
    }
}