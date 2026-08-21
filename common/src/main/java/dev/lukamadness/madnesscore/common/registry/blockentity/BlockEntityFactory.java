package dev.lukamadness.madnesscore.common.registry.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Reemplazo 100% comun de {@code BlockEntityType.BlockEntitySupplier}.
 * <p>
 * {@code BlockEntityType.BlockEntitySupplier} es package-private en vanilla;
 * NeoForge la vuelve publica via su propio AccessTransformer, pero eso solo
 * aplica al modulo neoforge (que usa el pipeline completo de NeoForge). El
 * modulo common compila contra NeoForm puro (sin los patches de NeoForge),
 * asi que no puede referenciarla directamente. Esta interfaz tiene
 * exactamente la misma forma y se adapta a la real en cada plataforma.
 */
@FunctionalInterface
public interface BlockEntityFactory<T extends BlockEntity> {
    T create(BlockPos pos, BlockState state);
}
