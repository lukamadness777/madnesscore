package dev.lukamadness.madnesscore.common.client.compat.jade;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.identity.NameVisibilityApi;
import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlock;
import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.blocks.CompressorBlock;
import dev.lukamadness.madnesscore.common.content.technology.blocks.CompressorBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.blocks.EnergyConverterBlock;
import dev.lukamadness.madnesscore.common.content.technology.blocks.EnergyConverterBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.blocks.HeatGeneratorBlock;
import dev.lukamadness.madnesscore.common.content.technology.blocks.HeatGeneratorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/**
 * Integración con Jade: cuando NameVisibilityApi dice que el nombre de la entidad
 * está oculto, reemplaza la línea de nombre (que Jade siempre agrega, isRequired=true)
 * por "???" antes de que se agreguen el resto de las líneas (mod name, distancia, etc.).
 * <p>
 * También registra los tooltips de las máquinas de Madness Core (Compressor, Alloy
 * Smeltery, Energy Converter, Heat Generator): cada una tiene un provider de datos de
 * servidor (register -> IWailaCommonRegistration, corre server-side) y uno de tooltip
 * de cliente (registerClient -> IWailaClientRegistration), separados porque desde Jade
 * 1.21.6 una misma clase ya no puede implementar ambas interfaces a la vez. Estos block
 * entities no sincronizan su contenido al cliente por su cuenta, por eso hace falta el
 * paso server data -> CompoundTag -> accessor.getServerData().
 * <p>
 * La Tailoring Table no tiene provider propio: Jade ya muestra el item por defecto,
 * así que no hace falta duplicarlo.
 */
@WailaPlugin
public class MadnessCoreJadePlugin implements IWailaPlugin {

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "hidden_name");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(HiddenNameProvider.INSTANCE, LivingEntity.class);

        registration.registerBlockComponent(CompressorJadeProvider.ComponentProvider.INSTANCE, CompressorBlock.class);
        registration.registerBlockComponent(AlloySmelteryJadeProvider.ComponentProvider.INSTANCE, AlloySmelteryBlock.class);
        registration.registerBlockComponent(EnergyConverterJadeProvider.ComponentProvider.INSTANCE, EnergyConverterBlock.class);
        registration.registerBlockComponent(HeatGeneratorJadeProvider.ComponentProvider.INSTANCE, HeatGeneratorBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CompressorJadeProvider.ServerData.INSTANCE, CompressorBlockEntity.class);
        registration.registerBlockDataProvider(AlloySmelteryJadeProvider.ServerData.INSTANCE, AlloySmelteryBlockEntity.class);
        registration.registerBlockDataProvider(EnergyConverterJadeProvider.ServerData.INSTANCE, EnergyConverterBlockEntity.class);
        registration.registerBlockDataProvider(HeatGeneratorJadeProvider.ServerData.INSTANCE, HeatGeneratorBlockEntity.class);
    }

    private enum HiddenNameProvider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            Entity entity = accessor.getEntity();
            if (entity instanceof LivingEntity living && NameVisibilityApi.isNameHidden(living)) {
                tooltip.clear();
                tooltip.add(Component.literal("???"));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public int getDefaultPriority() {
            return -10099; // justo después de ObjectNameProvider (-10100)
        }
    }
}
