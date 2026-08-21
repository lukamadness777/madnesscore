// common/tecnology/blocks/AlloySmelteryBlock.java
package dev.lukamadness.madnesscore.common.content.technology.blocks;

import com.mojang.serialization.MapCodec;
import dev.lukamadness.madnesscore.common.registry.blockentity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Recreación de IndustrialSmelterBlock, renombrado a Alloy Smeltery. Funde y fusiona
 * hasta 4 items de input en hasta 4 outputs, corriendo a Energy (no Heat).
 */
public class AlloySmelteryBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    // --- Voxel shapes, calcadas del modelo original (industrial_smelter_base.json) ---
    // Simétricas bajo rotación de 90° (forman un "anillo" cuadrado), idénticas en las 4
    // orientaciones. Solo la "boca" (apertura frontal) cambia de lado.
    private static VoxelShape ringShape() {
        return Stream.of(
                Block.box(1, 0, 1, 15, 5, 15),   // base
                Block.box(3, 5, 3, 11, 16, 5),   // pared
                Block.box(11, 5, 3, 13, 16, 11), // pared
                Block.box(5, 5, 11, 13, 16, 13),  // pared
                Block.box(3, 5, 5, 5, 16, 13)     // pared
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    }

    private static final VoxelShape RING = ringShape();

    private static final VoxelShape MOUTH_SOUTH = Block.box(3, 0, 14, 13, 14, 16);
    private static final VoxelShape MOUTH_NORTH = Block.box(3, 0, 0, 13, 14, 2);
    private static final VoxelShape MOUTH_WEST  = Block.box(0, 0, 3, 2, 14, 13);
    private static final VoxelShape MOUTH_EAST  = Block.box(14, 0, 3, 16, 14, 13);

    public static final VoxelShape SOUTH_SHAPE = Shapes.join(RING, MOUTH_SOUTH, BooleanOp.OR);
    public static final VoxelShape NORTH_SHAPE = Shapes.join(RING, MOUTH_NORTH, BooleanOp.OR);
    public static final VoxelShape WEST_SHAPE  = Shapes.join(RING, MOUTH_WEST, BooleanOp.OR);
    public static final VoxelShape EAST_SHAPE  = Shapes.join(RING, MOUTH_EAST, BooleanOp.OR);

    public AlloySmelteryBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(AlloySmelteryBlock::new);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlloySmelteryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ALLOY_SMELTERY.get(), AlloySmelteryBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AlloySmelteryBlockEntity smelter) {
            player.openMenu(smelter);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof AlloySmelteryBlockEntity be) {
                Containers.dropContents(level, pos, be);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> SOUTH_SHAPE;
        };
    }
}