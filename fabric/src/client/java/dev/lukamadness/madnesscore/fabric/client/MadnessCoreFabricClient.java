package dev.lukamadness.madnesscore.fabric.client;

import dev.lukamadness.madnesscore.common.client.MadnessCoreCommonClient;
import dev.lukamadness.madnesscore.common.client.item.color.ModItemColors;
import dev.lukamadness.madnesscore.common.client.registry.block.ModBlockRenderTypes;
import dev.lukamadness.madnesscore.common.client.screen.MadnessCoreScreenButtons;
import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;
import dev.lukamadness.madnesscore.fabric.appearance.network.FabricAppearanceNetwork;
import dev.lukamadness.madnesscore.fabric.client.config.FabricConfigEntryPointDiscovery;
import dev.lukamadness.madnesscore.fabric.client.dimension.ClientPacketHandlers;
import dev.lukamadness.madnesscore.fabric.client.registry.fluid.FabricFluidRenderRegistrar;
import dev.lukamadness.madnesscore.fabric.client.slots.network.FabricClientSlotNetworking;
import dev.lukamadness.madnesscore.fabric.client.appearance.network.FabricClientAppearanceNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public class MadnessCoreFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricAppearanceNetwork.setClientSender(payload -> {
            if (ClientPlayNetworking.canSend(AppearanceConfigPayload.TYPE)) {
                ClientPlayNetworking.send(payload);
            }
        });

        FabricClientSlotNetworking.init();
        FabricClientAppearanceNetworking.init();
        MadnessCoreCommonClient.init();
        FabricConfigEntryPointDiscovery.discoverAndFinalize();
        FabricFluidRenderRegistrar.register();
        ClientPacketHandlers.register();
        ModItemColors.registerAll(ColorProviderRegistry.ITEM::register);

        ModBlockRenderTypes.registerAll((type, blocks) -> {
            for (Block block : blocks) {
                BlockRenderLayerMap.INSTANCE.putBlock(block, type);
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, w, h) ->
                MadnessCoreScreenButtons.onScreenInit(screen, screen::addRenderableWidget));
    }
}
