package dev.lukamadness.madnesscore.fabric.client.slots.network;

import dev.lukamadness.madnesscore.common.slots.network.SlotBreakPayload;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotComponentPayload;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotDefinitionsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Contraparte cliente (Fase 4) de {@code FabricSlotNetworking}: registra los receivers de
 * {@link SyncSlotComponentPayload} y {@link SyncSlotDefinitionsPayload} (S2C).
 */
public final class FabricClientSlotNetworking {

    private FabricClientSlotNetworking() {
    }

    /**
     * Llamar UNA vez desde {@code MadnessCoreFabric#onInitializeClient} (client).
     * <p>
     * OJO: no envolver estas llamadas en {@code context.client().execute(...)}. Fabric API ya
     * ejecuta el receiver dentro de {@code client.execute(...)} internamente (ver
     * {@code ClientPlayNetworkAddon#receive}), asi que un segundo {@code execute(...)} aca
     * dentro NO corre en el mismo tick: se reencola al FINAL de la cola de tareas del hilo
     * principal. Eso deja pasar primero el reenvio de {@code ClientboundContainerSetContentPacket}
     * (que llega justo despues por el mismo canal y ya esta encolado ANTES de esta segunda
     * vuelta), y ese paquete se procesa contra el {@code InventoryMenu} todavia viejo (sin
     * rebuild) -> IndexOutOfBoundsException -> "Network Protocol Error" -> desconexion. Por eso
     * pasaba solo en Fabric y no en NeoForge (alli {@code context.enqueueWork()} es el UNICO
     * encolado, porque ese handler corre en el hilo de red, no en el principal).
     */
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SyncSlotComponentPayload.TYPE, (payload, context) ->
                SlotNetworking.handleSyncOnClient(payload, context.client().level));

        // Sin esto SlotsApi.getClientEntityLoader() (usado por el mixin de tooltip y por el hover
        // de slots en pantalla) queda vacio para siempre: el cliente no puede leer la carpeta
        // "data/" de los data packs (su ReloadableResourceManager solo ve "assets/"). Se pasa
        // context.player() para que handleDefinitionsSyncOnClient pueda forzar el rebuild del
        // InventoryMenu local (ver el javadoc del metodo para el porque hace falta).
        ClientPlayNetworking.registerGlobalReceiver(SyncSlotDefinitionsPayload.TYPE, (payload, context) ->
                SlotNetworking.handleDefinitionsSyncOnClient(payload, context.player()));

        // "Fire and forget": reproduce el efecto de rotura (sonido + particulas) de un item
        // equipado en un slot dinamico. Portado del receiver de BreakPayload en TrinketsClient.
        ClientPlayNetworking.registerGlobalReceiver(SlotBreakPayload.TYPE, (payload, context) ->
                SlotNetworking.handleBreakOnClient(payload, context.client().level));
    }
}