package dev.lukamadness.madnesscore.common.client.registry.block;

import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public final class ModBlockRenderTypes {
    private ModBlockRenderTypes() {
    }

    @FunctionalInterface
    public interface BlockRenderTypeRegistrar {
        void register(RenderType type, Block... blocks);
    }

    public static void registerAll(BlockRenderTypeRegistrar registrar) {
        registrar.register(RenderType.cutout(),
                ModBlocks.ALLOY_SMELTERY.get(),
                ModBlocks.HEAT_GENERATOR.get(),
                ModBlocks.ENERGY_CONVERTER.get(),
                ModBlocks.COMPRESSOR.get(),
                ModBlocks.TAILORING_TABLE.get());
    }
}
