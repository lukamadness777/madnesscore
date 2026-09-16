package dev.lukamadness.madnesscore.neoforge.slots;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.slots.SlotDeathHandler;
import dev.lukamadness.madnesscore.common.slots.SlotRespawnHandler;
import dev.lukamadness.madnesscore.common.slots.SlotTicker;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
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

public class SlotEventListener {
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

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer
                && event.getOriginal() instanceof ServerPlayer oldPlayer) {
            SlotRespawnHandler.onRespawn(oldPlayer, newPlayer);
            SlotRespawnHandler.markPendingDimensionResync(newPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SlotRespawnHandler.markPendingDimensionResync(player);
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity trackedEntity
                && event.getEntity() instanceof ServerPlayer viewer) {
            SlotNetworking.sendFullSyncTo(trackedEntity, viewer);
        }
    }

    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(SlotsApi.getServerGroupLoader());
        event.addListener(SlotsApi.getServerEntityLoader());
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        SyncSlotDefinitionsPayload payload = SlotNetworking.buildDefinitionsPayload();
        event.getRelevantPlayers().forEach(player -> {
            if (player.inventoryMenu instanceof PlayerSlotMenu menu) {
                menu.madnesscore$updateSlots(true);
            }

            PacketDistributor.sendToPlayer(player, payload);

            player.inventoryMenu.sendAllDataToRemote();
        });
    }
}
