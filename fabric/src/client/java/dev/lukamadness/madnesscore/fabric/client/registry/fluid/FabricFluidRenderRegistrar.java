package dev.lukamadness.madnesscore.fabric.client.registry.fluid;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.fabric.registry.fluid.FabricFluidRegistryHelper;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;

/**
 * Registra el FluidRenderHandler (texturas + tinte) para todos los fluidos
 * registrados via FluidRegistryHelper.INSTANCE.registerFluid(...) en Fabric.
 * <p>
 * Llamar a {@link #register()} desde el ClientModInitializer de Fabric
 * (dev.lukamadness.madnesscore.fabric.client.MadnessCoreFabric#onInitializeClient),
 * despues de que MadnessCoreCommon.init() ya corrio.
 */
public final class FabricFluidRenderRegistrar {
    private FabricFluidRenderRegistrar() {
    }

    public static void register() {
        FabricFluidRegistryHelper.getPendingRenderEntries().forEach((id, entry) -> {
            var handler = new SimpleFluidRenderHandler(
                    entry.properties().stillTexture(),
                    entry.properties().flowingTexture(),
                    entry.properties().tintColor()
            );
            FluidRenderHandlerRegistry.INSTANCE.register(entry.source(), entry.flowing(), handler);
            MadnessCoreCommon.LOG.info("Registered fluid render handler for {}", id);
        });
    }
}
