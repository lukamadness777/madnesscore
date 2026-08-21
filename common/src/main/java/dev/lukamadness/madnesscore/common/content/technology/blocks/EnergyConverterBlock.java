package dev.lukamadness.madnesscore.common.content.technology.blocks;

import com.mojang.serialization.MapCodec;
import dev.lukamadness.madnesscore.common.registry.blockentity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;


/**
 * El bloque puente Heat -> Energy del diseño: recibe Heat de un vecino (típicamente un
 * Heat Generator) y empuja Energy a otro (típicamente una mesa como el futuro
 * Compressor). No tiene inventario, pero sí una pantalla puramente informativa al hacer
 * click derecho (ver EnergyConverterScreenHandler). Ver EnergyConverterBlockEntity para
 * la lógica.
 */
public class EnergyConverterBlock extends BaseEntityBlock {

    public EnergyConverterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(EnergyConverterBlock::new);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyConverterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.ENERGY_CONVERTER.get(), EnergyConverterBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof EnergyConverterBlockEntity converter) {
            player.openMenu(converter);
        }
        return InteractionResult.CONSUME;
    }
}