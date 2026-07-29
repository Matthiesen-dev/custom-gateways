package dev.matthiesen.custom_gateways.common.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import dev.matthiesen.custom_gateways.common.block.entity.RemoteGatewayBlockEntity;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class RemoteGatewayBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty IS_TOP = BooleanProperty.create("is_top");
    private final Map<Direction, VoxelShape> shapes = Maps.newEnumMap(Direction.class);
    private final VoxelShape baseShape;

    public RemoteGatewayBlock() {
        super(BlockBehaviour.Properties.of()
            .noCollission()
            .strength(-1.0f, 3_600_000.0f)
            .noLootTable()
            .pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(IS_TOP, false));
        this.baseShape = createShape();
        initializeShapes();
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        if (state.getValue(IS_TOP)) {
            return RenderShape.INVISIBLE;
        }
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @NotNull BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (state.getValue(IS_TOP)) {
            return;
        }

        BlockPos topPos = pos.above();
        BlockState topState = this.defaultBlockState()
            .setValue(IS_TOP, true)
            .setValue(FACING, state.getValue(FACING));
        level.setBlock(topPos, topState, 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, IS_TOP);
    }

    private VoxelShape createShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.4375, 1, 1, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1, 0.4375, 1, 2, 0.5625), BooleanOp.OR);
        return shape;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(IS_TOP)) {
            return Shapes.empty();
        }
        return shapes.getOrDefault(state.getValue(FACING), baseShape);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState nextState, boolean isMoving) {
        if (state.getBlock() == nextState.getBlock()) {
            super.onRemove(state, level, pos, nextState, isMoving);
            return;
        }

        BlockPos counterpartPos = state.getValue(IS_TOP) ? pos.below() : pos.above();
        BlockState counterpart = level.getBlockState(counterpartPos);
        if (counterpart.is(this) && counterpart.getValue(IS_TOP) != state.getValue(IS_TOP)) {
            level.removeBlock(counterpartPos, false);
        }

        super.onRemove(state, level, pos, nextState, isMoving);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(IS_TOP)) {
            return null;
        }
        return BlockEntityRegistry.REMOTE_GATEWAY_BE.get().create(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || state.getValue(IS_TOP)) {
            return null;
        }
        if (type == BlockEntityRegistry.REMOTE_GATEWAY_BE.get()) {
            return RemoteGatewayBlockEntity::tick;
        }
        return null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    private void initializeShapes() {
        shapes.put(Direction.NORTH, baseShape);
        shapes.put(Direction.SOUTH, calculateRotation(Direction.SOUTH, baseShape));
        shapes.put(Direction.EAST, calculateRotation(Direction.EAST, baseShape));
        shapes.put(Direction.WEST, calculateRotation(Direction.WEST, baseShape));
    }

    private static VoxelShape calculateRotation(Direction direction, VoxelShape base) {
        VoxelShape[] buffer = new VoxelShape[]{base, Shapes.empty()};
        int times = (direction.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                buffer[1] = Shapes.joinUnoptimized(buffer[1],
                    Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX), BooleanOp.OR));
            buffer[0] = buffer[1].optimize();
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }
}

