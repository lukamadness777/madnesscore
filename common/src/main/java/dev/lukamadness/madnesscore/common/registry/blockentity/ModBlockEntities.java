package dev.lukamadness.madnesscore.common.registry.blockentity;

import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.blocks.EnergyConverterBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.blocks.HeatGeneratorBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * Registro multiplataforma de BlockEntityType.
 * <p>
 * Ejemplo de uso:
 * <pre>{@code
 * public static final Supplier<BlockEntityType<ExampleBlockEntity>> EXAMPLE_BE = register(
 *         "example_block_entity",
 *         ExampleBlockEntity::new,
 *         () -> new Block[]{ ModBlocks.EXAMPLE_BLOCK.get() }
 * );
 * }</pre>
 */
public class ModBlockEntities {

    private static <T extends BlockEntity> Supplier<BlockEntityType<T>> register(
            String id,
            BlockEntityFactory<T> factory,
            Supplier<Block[]> validBlocks
    ) {
        return RegistryHelper.INSTANCE.registerBlockEntity(id, factory, validBlocks);
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

    public static void init() {
        // Fuerza la carga de la clase para que los Supplier de arriba se ejecuten
    }
}