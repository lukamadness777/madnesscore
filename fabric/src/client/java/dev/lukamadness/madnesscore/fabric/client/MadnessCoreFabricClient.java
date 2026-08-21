package dev.lukamadness.madnesscore.fabric.client;

import dev.lukamadness.madnesscore.common.client.MadnessCoreCommonClient;
import dev.lukamadness.madnesscore.fabric.client.registry.fluid.FabricFluidRenderRegistrar;
import dev.lukamadness.madnesscore.fabric.client.slots.network.FabricClientSlotNetworking;
import net.fabricmc.api.ClientModInitializer;

public class MadnessCoreFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricClientSlotNetworking.init();
        MadnessCoreCommonClient.init();
        FabricFluidRenderRegistrar.register();
    }
}
