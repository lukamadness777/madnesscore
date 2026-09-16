package dev.lukamadness.madnesscore.common.content.technology.blocks;

import com.mojang.serialization.MapCodec;
import dev.lukamadness.madnesscore.common.registry.blockentity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class EnergyConverterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final double[][] STRAIGHT = {
            {0, 0, 0, 16, 16, 16},
            {4, 16, 3, 12, 17, 4},
            {4, 16, 12, 12, 17, 13},
            {3, 16, 3, 4, 17, 13},
            {12, 16, 3, 13, 17, 13},
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
            {0, 0, 0, 16, 16, 16},

            {4, 2, -1, 12, 3, 0},
            {4, 8, -1, 12, 9, 0},
            {4, 3, -1, 5, 8, 0},
            {11, 3, -1, 12, 8, 0},
            {4, 10, -0.1, 12, 14, 0.2},

            {4, 16, 3, 12, 17, 4},
            {4, 16, 12, 12, 17, 13},
            {3, 16, 3, 4, 17, 13},
            {12, 16, 3, 13, 17, 13},

            {-1, 5, 2, 0, 6, 14},
            {-1, 7, 2, 0, 8, 14},
            {-1, 9, 2, 0, 10, 14},
            {-1, 11, 2, 0, 12, 14},
            {-1, 13, 2, 0, 14, 14},

            {16, 5, 2, 17, 6, 14},
            {16, 7, 2, 17, 8, 14},
            {16, 9, 2, 17, 10, 14},
            {16, 11, 2, 17, 12, 14},
            {16, 13, 2, 17, 14, 14},
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
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = Shapes.or(buffer[1], Shapes.box(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

    public EnergyConverterBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(EnergyConverterBlock::new);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(LIT, false);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINES_BY_FACING.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES_BY_FACING.get(state.getValue(FACING));
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
