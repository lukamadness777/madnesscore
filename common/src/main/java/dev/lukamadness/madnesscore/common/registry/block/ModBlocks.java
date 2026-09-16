package dev.lukamadness.madnesscore.common.registry.block;

import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlock;
import dev.lukamadness.madnesscore.common.content.technology.blocks.CompressorBlock;
import dev.lukamadness.madnesscore.common.content.technology.blocks.EnergyConverterBlock;
import dev.lukamadness.madnesscore.common.content.technology.blocks.HeatGeneratorBlock;
import dev.lukamadness.madnesscore.common.content.tailoring.blocks.TailoringTableBlock;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
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

    public static final Supplier<CompressorBlock> COMPRESSOR = register("compressor",
            () -> new CompressorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(CompressorBlock.LIT) ? 13 : 0)));

    public static final Supplier<TailoringTableBlock> TAILORING_TABLE = register("tailoring_table",
            () -> new TailoringTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD)));

    public static <T extends Block> Supplier<T> register(String id, Supplier<T> block) {
        return RegistryHelperLoader.INSTANCE.registerBlock(id, block);
    }

    public static <T extends Block> Supplier<T> register(String namespace, String id, Supplier<T> block) {
        return RegistryHelperLoader.INSTANCE.registerBlock(namespace, id, block);
    }

    public static void init() {
    }
}
