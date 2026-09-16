package dev.lukamadness.madnesscore.common.registry.blockentity;

import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.blocks.CompressorBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.blocks.EnergyConverterBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.blocks.HeatGeneratorBlockEntity;
import dev.lukamadness.madnesscore.common.content.tailoring.blocks.TailoringTableBlockEntity;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> register(
            String id,
            BlockEntityFactory<T> factory,
            Supplier<Block[]> validBlocks
    ) {
        return RegistryHelperLoader.INSTANCE.registerBlockEntity(id, factory, validBlocks);
    }

    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> register(
            String namespace,
            String id,
            BlockEntityFactory<T> factory,
            Supplier<Block[]> validBlocks
    ) {
        return RegistryHelperLoader.INSTANCE.registerBlockEntity(namespace, id, factory, validBlocks);
    }

    public static final Supplier<BlockEntityType<HeatGeneratorBlockEntity>> HEAT_GENERATOR = register(
            "heat_generator",
            HeatGeneratorBlockEntity::new,
            () -> new Block[]{ ModBlocks.HEAT_GENERATOR.get() }
    );

    public static final Supplier<BlockEntityType<AlloySmelteryBlockEntity>> ALLOY_SMELTERY = register(
            "alloy_smeltery",
            AlloySmelteryBlockEntity::new,
            () -> new Block[]{ ModBlocks.ALLOY_SMELTERY.get() }
    );

    public static final Supplier<BlockEntityType<EnergyConverterBlockEntity>> ENERGY_CONVERTER = register(
            "energy_converter",
            EnergyConverterBlockEntity::new,
            () -> new Block[]{ ModBlocks.ENERGY_CONVERTER.get() }
    );

    public static final Supplier<BlockEntityType<CompressorBlockEntity>> COMPRESSOR = register(
            "compressor",
            CompressorBlockEntity::new,
            () -> new Block[]{ ModBlocks.COMPRESSOR.get() }
    );

    public static final Supplier<BlockEntityType<TailoringTableBlockEntity>> TAILORING_TABLE = register(
            "tailoring_table",
            TailoringTableBlockEntity::new,
            () -> new Block[]{ ModBlocks.TAILORING_TABLE.get() }
    );

    public static void init() {
    }
}
