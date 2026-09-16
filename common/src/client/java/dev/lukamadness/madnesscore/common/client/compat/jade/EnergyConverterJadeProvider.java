package dev.lukamadness.madnesscore.common.client.compat.jade;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.technology.blocks.EnergyConverterBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.energy.EnergyFormat;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFormat;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFuelRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/**
 * Muestra en Jade el calor disponible y la energía disponible del Energy Converter.
 * <p>
 * Separado en dos clases (server data / component) porque desde Jade 1.21.6 una misma
 * clase ya no puede implementar IServerDataProvider e IComponentProvider a la vez.
 */
final class EnergyConverterJadeProvider {
    private EnergyConverterJadeProvider() {}

    static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "energy_converter");

    enum ServerData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof EnergyConverterBlockEntity entity)) {
                return;
            }
            tag.putDouble("Temperature", entity.getHeatStorage().getTemperature());
            tag.putDouble("MaxTemperature", HeatFuelRegistry.getMaxHeatTemperature(accessor.getLevel()));
            tag.putInt("Energy", entity.getEnergyStorage().getEnergy());
            tag.putInt("EnergyCapacity", entity.getEnergyStorage().getCapacity());
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
            if (!(accessor.getBlockEntity() instanceof EnergyConverterBlockEntity)) {
                return;
            }
            CompoundTag data = accessor.getServerData();
            if (!data.contains("Energy")) {
                return;
            }

            String temperature = HeatFormat.formatTemperature(data.getDouble("Temperature"));
            String maxTemperature = HeatFormat.formatTemperature(data.getDouble("MaxTemperature"));
            tooltip.add(Component.translatable("madnesscore.jade.energy_converter.heat")
                    .append(": " + temperature + "/" + maxTemperature)
                    .withStyle(ChatFormatting.GOLD));

            String energy = EnergyFormat.format(data.getInt("Energy"));
            String capacity = EnergyFormat.format(data.getInt("EnergyCapacity"));
            tooltip.add(Component.translatable("madnesscore.jade.energy_converter.energy")
                    .append(": " + energy + "/" + capacity)
                    .withStyle(ChatFormatting.BLUE));
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
