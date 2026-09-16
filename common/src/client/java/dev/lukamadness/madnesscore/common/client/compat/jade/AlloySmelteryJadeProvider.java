package dev.lukamadness.madnesscore.common.client.compat.jade;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFormat;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFuelRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/**
 * Muestra en Jade el calor actual del Alloy Smeltery. Los items de entrada/salida no se
 * muestran porque Jade ya expone el item del bloque por defecto.
 * <p>
 * Separado en dos clases (server data / component) porque desde Jade 1.21.6 una misma
 * clase ya no puede implementar IServerDataProvider e IComponentProvider a la vez.
 */
final class AlloySmelteryJadeProvider {
    private AlloySmelteryJadeProvider() {}

    static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "alloy_smeltery");

    enum ServerData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof AlloySmelteryBlockEntity entity)) {
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
            if (!(accessor.getBlockEntity() instanceof AlloySmelteryBlockEntity)) {
                return;
            }
            CompoundTag data = accessor.getServerData();
            if (!data.contains("Temperature")) {
                return;
            }

            String temperature = HeatFormat.formatTemperature(data.getDouble("Temperature"));
            String maxTemperature = HeatFormat.formatTemperature(data.getDouble("MaxTemperature"));
            tooltip.add(Component.translatable("madnesscore.jade.alloy_smeltery.heat")
                    .append(": " + temperature + "/" + maxTemperature)
                    .withStyle(ChatFormatting.GOLD));
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
