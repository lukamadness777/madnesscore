package dev.lukamadness.madnesscore.fabric;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.slots.data.EntitySlotReloadListener;
import dev.lukamadness.madnesscore.common.slots.data.SlotGroupReloadListener;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import dev.lukamadness.madnesscore.fabric.slots.data.FabricIdentifiableReloadListener;
import dev.lukamadness.madnesscore.fabric.slots.network.FabricSlotNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

import java.util.List;

public class MadnessCoreFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MadnessCoreCommon.LOG.info("Hello Fabric world!");
        FabricSlotNetworking.init();
        MadnessCoreCommon.init();
        registerSlotDataReloadListeners();
        registerSlotDataSync();
    }

    /**
     * Sin esto, {@code SlotGroupReloadListener}/{@code EntitySlotReloadListener} nunca se enteran
     * de ningun reload de data pack: sus mapas internos quedan vacios para siempre y
     * {@code SlotsApi.getEntitySlots(...)} nunca devuelve nada, en ningun lado. Se registran en
     * orden (grupos antes que entidades) porque {@code EntitySlotReloadListener#apply} necesita
     * los grupos ya cargados.
     */
    private void registerSlotDataReloadListeners() {
        ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.SERVER_DATA);

        helper.registerReloadListener(SlotGroupReloadListener.ID, provider ->
                new FabricIdentifiableReloadListener(SlotsApi.getServerGroupLoader(), SlotGroupReloadListener.ID, List.of()));

        helper.registerReloadListener(EntitySlotReloadListener.ID, provider ->
                new FabricIdentifiableReloadListener(SlotsApi.getServerEntityLoader(), EntitySlotReloadListener.ID,
                        List.of(SlotGroupReloadListener.ID)));
    }

    /**
     * El cliente no puede leer la carpeta "data/" de los data packs (ver comentario en
     * {@code FabricClientSlotNetworking}), asi que le mandamos las definiciones resueltas por
     * red. {@code SYNC_DATA_PACK_CONTENTS} cubre tanto el login de un jugador como un
     * {@code /reload} en curso (equivalente a {@code OnDatapackSyncEvent} de NeoForge).
     */
    private void registerSlotDataSync() {
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            // Rebuild del lado SERVIDOR: madnesscore$init ya NO agrega los slots dinamicos al
            // construir el InventoryMenu (ver el javadoc largo en MixinInventoryMenu para el
            // porque). Los agregamos reciennaca, justo antes de mandar la definicion, para que
            // el servidor pase de 0 a N dinamicos exactamente en el mismo momento en que el
            // cliente va a poder hacerlo (al recibir el paquete de abajo).
            if (player.inventoryMenu instanceof PlayerSlotMenu menu) {
                menu.madnesscore$updateSlots(true);
            }

            ServerPlayNetworking.send(player, SlotNetworking.buildDefinitionsPayload());

            // Reenvio obligatorio: el ClientboundContainerSetContentPacket inicial de login ya se
            // mando con el InventoryMenu del servidor todavia en su base vainilla (46 slots, sin
            // dinamicos), asi que coincidio con el del cliente y no crasheo. Pero ahora que
            // ambos lados ya saben la cantidad real (linea de arriba en el servidor, el receiver
            // de SyncSlotDefinitionsPayload en el cliente), hace falta este sendAllDataToRemote()
            // (metodo vainilla publico) para que el cliente reciba el contenido actualizado. El
            // canal preserva el orden de los paquetes en la misma conexion, asi que este reenvio
            // le llega al cliente DESPUES de haber aplicado la definicion de arriba.
            player.inventoryMenu.sendAllDataToRemote();
        });
    }
}