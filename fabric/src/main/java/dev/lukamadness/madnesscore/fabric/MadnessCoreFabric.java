package dev.lukamadness.madnesscore.fabric;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.dimension.DynamicDimensionApi;
import dev.lukamadness.madnesscore.common.content.tailoring.network.TailoringCandidatesPacket;
import dev.lukamadness.madnesscore.common.network.UpdateDimensionsPacket;
import dev.lukamadness.madnesscore.common.slots.SlotRespawnHandler;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.slots.data.EntitySlotReloadListener;
import dev.lukamadness.madnesscore.common.slots.data.SlotGroupReloadListener;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import dev.lukamadness.madnesscore.fabric.command.MadnessCoreFabricCommands;
import dev.lukamadness.madnesscore.fabric.dimension.DimensionManager;
import dev.lukamadness.madnesscore.fabric.appearance.network.FabricAppearanceNetworking;
import dev.lukamadness.madnesscore.fabric.event.bloodline.BloodlineFabricEvents;
import dev.lukamadness.madnesscore.fabric.event.family.FamilyFabricEvents;
import dev.lukamadness.madnesscore.fabric.event.species.SpeciesFabricEvents;
import dev.lukamadness.madnesscore.fabric.event.villager.VillagerFabricEvents;
import dev.lukamadness.madnesscore.fabric.slots.data.FabricIdentifiableReloadListener;
import dev.lukamadness.madnesscore.fabric.slots.network.FabricSlotNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

import java.util.List;

public class MadnessCoreFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MadnessCoreCommon.LOG.info("Hello Fabric world!");
        FabricSlotNetworking.init();
        FabricAppearanceNetworking.init();
        MadnessCoreCommon.init();
        registerSlotDataReloadListeners();
        registerSlotDataSync();
        registerSlotRespawnSync();
        registerSlotDimensionChangeSync();
        SpeciesFabricEvents.register();
        BloodlineFabricEvents.register();
        FamilyFabricEvents.register();
        VillagerFabricEvents.register();
        MadnessCoreFabricCommands.register();

        DynamicDimensionApi.setInstance(DimensionManager.INSTANCE);

        PayloadTypeRegistry.playS2C().register(UpdateDimensionsPacket.TYPE, UpdateDimensionsPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(TailoringCandidatesPacket.TYPE, TailoringCandidatesPacket.STREAM_CODEC);

        ServerTickEvents.END_SERVER_TICK.register(server ->
                DimensionManager.INSTANCE.unregisterScheduledDimensions(server));

        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
                DimensionManager.INSTANCE.clearPendingUnregistrations());
    }

    private void registerSlotDataReloadListeners() {
        ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.SERVER_DATA);

        helper.registerReloadListener(SlotGroupReloadListener.ID, provider ->
                new FabricIdentifiableReloadListener(SlotsApi.getServerGroupLoader(), SlotGroupReloadListener.ID, List.of()));

        helper.registerReloadListener(EntitySlotReloadListener.ID, provider ->
                new FabricIdentifiableReloadListener(SlotsApi.getServerEntityLoader(), EntitySlotReloadListener.ID,
                        List.of(SlotGroupReloadListener.ID)));
    }

    private void registerSlotDataSync() {
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if (player.inventoryMenu instanceof PlayerSlotMenu menu) {
                menu.madnesscore$updateSlots(true);
            }

            ServerPlayNetworking.send(player, SlotNetworking.buildDefinitionsPayload());

            player.inventoryMenu.sendAllDataToRemote();
        });
    }

    private void registerSlotRespawnSync() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            SlotRespawnHandler.onRespawn(oldPlayer, newPlayer);
            SlotRespawnHandler.markPendingDimensionResync(newPlayer);
        });
    }

    private void registerSlotDimensionChangeSync() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            SlotRespawnHandler.markPendingDimensionResync(player);
        });
    }
}
