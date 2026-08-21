package dev.lukamadness.madnesscore.neoforge.slots;

import dev.lukamadness.madnesscore.common.slots.SlotDeathHandler;
import dev.lukamadness.madnesscore.common.slots.SlotTicker;
import dev.lukamadness.madnesscore.common.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.slots.data.EntitySlotReloadListener;
import dev.lukamadness.madnesscore.common.slots.data.SlotGroupReloadListener;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import dev.lukamadness.madnesscore.common.slots.network.SyncSlotDefinitionsPayload;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Conecta el bus de eventos de NeoForge con la logica common compartida de slots. Equivalente
 * NeoForge de los inject de {@code MixinLivingEntitySlots} en Fabric. Se registra en
 * {@code NeoForge.EVENT_BUS} (bus de juego, no el bus de mod) desde {@code MadnessCoreNeoForge}.
 */
public class SlotEventListener {

    /**
     * {@code LivingEvent.LivingTickEvent} ya no existe en NeoForge moderno: el tick de entidades
     * se movio a {@link EntityTickEvent}, que se dispara para TODAS las entidades (no solo
     * {@link LivingEntity}), asi que ahora hace falta filtrar con {@code instanceof}.
     */
    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            SlotTicker.tick(livingEntity);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel serverLevel) {
            SlotDeathHandler.dropOnDeath(entity, serverLevel);
        }
    }

    /**
     * Fase 4: cuando un jugador empieza a trackear una entidad (ej: entra en su rango de
     * visibilidad), le mandamos de una el estado completo de slots de esa entidad. Sin esto, un
     * observador nuevo solo veria el equipo actualizado recien cuando la entidad trackeada
     * cambie de equipo la proxima vez (SlotTicker#tick solo sincroniza on-change, no on-demand).
     */
    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity trackedEntity
                && event.getEntity() instanceof ServerPlayer viewer) {
            SlotNetworking.sendFullSyncTo(trackedEntity, viewer);
        }
    }

    /**
     * Sin esto, {@code SlotGroupReloadListener}/{@code EntitySlotReloadListener} nunca se enteran
     * de ningun reload de data pack: sus mapas internos quedan vacios para siempre y
     * {@code SlotsApi.getEntitySlots(...)} nunca devuelve nada, en ningun lado. Se agregan en
     * orden (grupos antes que entidades) porque {@code EntitySlotReloadListener#apply} necesita
     * los grupos ya cargados; {@link AddReloadListenerEvent#addListener} preserva el orden de
     * registro para listeners que no son {@code IdentifiableResourceReloadListener}.
     */
    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(SlotsApi.getServerGroupLoader());
        event.addListener(SlotsApi.getServerEntityLoader());
    }

    /**
     * El cliente no puede leer la carpeta "data/" de los data packs, asi que le mandamos las
     * definiciones resueltas por red. Este evento cubre tanto el login de un jugador como un
     * {@code /reload} en curso (ver javadoc de {@link OnDatapackSyncEvent}).
     */
    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        SyncSlotDefinitionsPayload payload = SlotNetworking.buildDefinitionsPayload();
        event.getRelevantPlayers().forEach(player -> {
            // Rebuild del lado SERVIDOR: madnesscore$init ya NO agrega los slots dinamicos al
            // construir el InventoryMenu (ver el javadoc largo en MixinInventoryMenu para el
            // porque). Los agregamos reciennaca, justo antes de mandar la definicion, para que
            // el servidor pase de 0 a N dinamicos exactamente en el mismo momento en que el
            // cliente va a poder hacerlo (al recibir el paquete de abajo).
            if (player.inventoryMenu instanceof PlayerSlotMenu menu) {
                menu.madnesscore$updateSlots(true);
            }

            PacketDistributor.sendToPlayer(player, payload);

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