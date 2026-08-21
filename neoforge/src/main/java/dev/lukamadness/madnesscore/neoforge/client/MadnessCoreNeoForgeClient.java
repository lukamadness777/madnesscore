package dev.lukamadness.madnesscore.neoforge.client;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.MadnessCoreCommonClient;
import dev.lukamadness.madnesscore.common.registry.fluid.ModFluidProperties;
import dev.lukamadness.madnesscore.neoforge.registry.fluid.NeoForgeFluidRegistryHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Map;

@EventBusSubscriber(modid = MadnessCoreCommon.MOD_ID, value = Dist.CLIENT)
public final class MadnessCoreNeoForgeClient {
    private MadnessCoreNeoForgeClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MadnessCoreCommonClient.init();
    }

    // Registra las texturas/tinte de cada fluido custom (equivalente al
    // FluidRenderHandlerRegistry de Fabric). Corre en el mod event bus, no
    // hace falta que este dentro de FMLClientSetupEvent.
    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        Map<String, ModFluidProperties> pending = NeoForgeFluidRegistryHelper.getPendingClientExtensions();
        Map<String, DeferredHolder<FluidType, FluidType>> types = NeoForgeFluidRegistryHelper.getFluidTypes();

        pending.forEach((id, properties) -> {
            FluidType fluidType = types.get(id).get();
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public net.minecraft.resources.ResourceLocation getStillTexture() {
                    return properties.stillTexture();
                }

                @Override
                public net.minecraft.resources.ResourceLocation getFlowingTexture() {
                    return properties.flowingTexture();
                }

                @Override
                public net.minecraft.resources.ResourceLocation getOverlayTexture() {
                    return properties.overlayTexture();
                }

                @Override
                public int getTintColor() {
                    return properties.tintColor();
                }
            }, fluidType);
            MadnessCoreCommon.LOG.info("Registered fluid client extensions for {}", id);
        });
    }
}