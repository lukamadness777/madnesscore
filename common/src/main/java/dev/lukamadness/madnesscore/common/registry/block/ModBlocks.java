package dev.lukamadness.madnesscore.common.registry.block;

import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlock;
import dev.lukamadness.madnesscore.common.content.technology.blocks.EnergyConverterBlock;
import dev.lukamadness.madnesscore.common.content.technology.blocks.HeatGeneratorBlock;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

public class ModBlocks {

    public static final Supplier<HeatGeneratorBlock> HEAT_GENERATOR = register("heat_generator",
            () -> new HeatGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(HeatGeneratorBlock.LIT) ? 13 : 0)));

    public static final Supplier<AlloySmelteryBlock> ALLOY_SMELTERY = register("alloy_smeltery",
            () -> new AlloySmelteryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(AlloySmelteryBlock.LIT) ? 13 : 0)));

    public static final Supplier<EnergyConverterBlock> ENERGY_CONVERTER = register("energy_converter",
            () -> new EnergyConverterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()));

    private static <T extends Block> Supplier<T> register(String id, Supplier<T> block) {
        return RegistryHelper.INSTANCE.registerBlock(id, block);
    }

    public static void init() {
        // Fuerza la carga de la clase para que los Supplier de arriba se ejecuten
    }
}