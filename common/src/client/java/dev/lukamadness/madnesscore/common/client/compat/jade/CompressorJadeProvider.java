package dev.lukamadness.madnesscore.common.client.compat.jade;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.technology.blocks.CompressorBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.energy.EnergyFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/**
 * Muestra en Jade la energía almacenada del Compressor. El item de entrada/salida no se
 * muestra porque Jade ya expone el item del bloque por defecto.
 * <p>
 * Separado en dos clases (server data / component) porque desde Jade 1.21.6 una misma
 * clase ya no puede implementar IServerDataProvider e IComponentProvider a la vez.
 */
final class CompressorJadeProvider {
    private CompressorJadeProvider() {}

    static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "compressor");

    enum ServerData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof CompressorBlockEntity entity)) {
                return;
            }
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
            if (!(accessor.getBlockEntity() instanceof CompressorBlockEntity)) {
                return;
            }
            CompoundTag data = accessor.getServerData();
            if (!data.contains("Energy")) {
                return;
            }

            String energy = EnergyFormat.format(data.getInt("Energy"));
            String capacity = EnergyFormat.format(data.getInt("EnergyCapacity"));
            tooltip.add(Component.translatable("madnesscore.jade.compressor.energy")
                    .append(": " + energy + "/" + capacity)
                    .withStyle(ChatFormatting.BLUE));
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
