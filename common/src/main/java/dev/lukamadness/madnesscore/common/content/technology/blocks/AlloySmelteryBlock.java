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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class AlloySmelteryBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final double[][] STRAIGHT = {
            {2, 9.9, 0, 14, 21.9, 16},
            {3, 7.9, 0, 13, 9.9, 1},
            {3, 7.9, 15, 13, 9.9, 16},
            {6.5, 10.4, 7.2, 15.5, 11.4, 8.2},
            {6.5, 20.4, 7.2, 15.5, 21.4, 8.2},
            {15.5, 10.4, 7.2, 16.5, 21.4, 8.2},
            {14.3, 14.4, 6.2, 17.7, 17.5, 9.2},
            {9.2, 14.4, -0.1, 12.2, 17.4, 1.1},
            {0, 0, 0, 16, 8, 16},
    };

    private static VoxelShape buildShape() {
        VoxelShape result = Shapes.empty();
        for (double[] b : STRAIGHT) {
            result = Shapes.or(result, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
        }
        return result.optimize();
    }

    public static final VoxelShape SHAPE_NORTH = buildShape();

    private static final double[][] OUTLINE = {
            {0, 0, 0, 16, 22, 16},
            {14, 10, 6, 18, 22, 9.5},
            {9, 14, -0.5, 12.5, 17.5, 1.5},
    };

    private static VoxelShape buildOutline() {
        VoxelShape result = Shapes.empty();
        for (double[] b : OUTLINE) {
            result = Shapes.or(result, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
        }
        return result.optimize();
    }

    public static final VoxelShape OUTLINE_NORTH = buildOutline();

    private static final Map<Direction, VoxelShape> SHAPES_BY_FACING = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> OUTLINES_BY_FACING = new EnumMap<>(Direction.class);

    static {
        SHAPES_BY_FACING.put(Direction.NORTH, SHAPE_NORTH);
        SHAPES_BY_FACING.put(Direction.SOUTH, rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_NORTH));
        SHAPES_BY_FACING.put(Direction.EAST, rotateShape(Direction.NORTH, Direction.EAST, SHAPE_NORTH));
        SHAPES_BY_FACING.put(Direction.WEST, rotateShape(Direction.NORTH, Direction.WEST, SHAPE_NORTH));

        OUTLINES_BY_FACING.put(Direction.NORTH, OUTLINE_NORTH);
        OUTLINES_BY_FACING.put(Direction.SOUTH, rotateShape(Direction.NORTH, Direction.SOUTH, OUTLINE_NORTH));
        OUTLINES_BY_FACING.put(Direction.EAST, rotateShape(Direction.NORTH, Direction.EAST, OUTLINE_NORTH));
        OUTLINES_BY_FACING.put(Direction.WEST, rotateShape(Direction.NORTH, Direction.WEST, OUTLINE_NORTH));
    }

    private static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};

        int times = (from.get2DDataValue() - to.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = Shapes.or(buffer[1], Shapes.box(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

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
        return OUTLINES_BY_FACING.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES_BY_FACING.get(state.getValue(FACING));
    }
}
