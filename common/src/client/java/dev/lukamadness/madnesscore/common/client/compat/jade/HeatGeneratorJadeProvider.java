package dev.lukamadness.madnesscore.common.client.compat.jade;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.technology.blocks.HeatGeneratorBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFormat;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFuelRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/**
 * Muestra en Jade el calor actual del Heat Generator. El combustible cargado no se
 * muestra porque Jade ya expone el item del bloque por defecto.
 * <p>
 * Separado en dos clases (server data / component) porque desde Jade 1.21.6 una misma
 * clase ya no puede implementar IServerDataProvider e IComponentProvider a la vez.
 */
final class HeatGeneratorJadeProvider {
    private HeatGeneratorJadeProvider() {}

    static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "heat_generator");

    enum ServerData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof HeatGeneratorBlockEntity entity)) {
                return;
            }
            tag.putDouble("Temperature", entity.getHeatStorage().getTemperature());
            tag.putDouble("MaxTemperature", HeatFuelRegistry.getMaxHeatTemperature(accessor.getLevel()));
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }

    enum ComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof HeatGeneratorBlockEntity)) {
                return;
            }
            CompoundTag data = accessor.getServerData();
            if (!data.contains("Temperature")) {
                return;
            }

            String temperature = HeatFormat.formatTemperature(data.getDouble("Temperature"));
            String maxTemperature = HeatFormat.formatTemperature(data.getDouble("MaxTemperature"));
            tooltip.add(Component.translatable("madnesscore.jade.heat_generator.heat")
                    .append(": " + temperature + "/" + maxTemperature)
                    .withStyle(ChatFormatting.GOLD));
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
