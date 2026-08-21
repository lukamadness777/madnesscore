package dev.lukamadness.madnesscore.common.registry.fluid;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;

import java.util.function.Supplier;

/**
 * Resultado de registrar un fluido: source, flowing, el LiquidBlock y
 * (opcionalmente) el bucket item. bucket() puede devolver null via el
 * Supplier si se registro con registerFluidNoBucket.
 */
public record FluidEntry<T extends SimpleFlowingFluid>(
        Supplier<T> source,
        Supplier<T> flowing,
        Supplier<LiquidBlock> block,
        Supplier<Item> bucket
) {
}
