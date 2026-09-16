package dev.lukamadness.madnesscore.common.registry.fluid;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;

import java.util.function.Supplier;

public record FluidEntry<T extends SimpleFlowingFluid>(
        Supplier<T> source,
        Supplier<T> flowing,
        Supplier<LiquidBlock> block,
        Supplier<Item> bucket
) {
}
