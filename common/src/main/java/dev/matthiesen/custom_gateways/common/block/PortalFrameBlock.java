package dev.matthiesen.custom_gateways.common.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class PortalFrameBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty IS_SLAVE = BooleanProperty.create("is_slave");

    private final Map<Direction, VoxelShape> shapes = Maps.newEnumMap(Direction.class);
    private final VoxelShape baseShape;

    public PortalFrameBlock() {
        super(Properties.of().noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(IS_SLAVE, false).setValue(FACING, Direction.NORTH));
        this.baseShape = createShape();
        initializeShapes();
    }

    private VoxelShape createShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.0625, 0.9375, 0.0625, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.1875, 0.8125, 0.125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.0625, 0.4375, 0.9375, 0.375, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.4375, 0.1875, 0.375, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.84375, 0.0625, 0.1875, 0.90625, 0.4375, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.09375, 0.0625, 0.1875, 0.15625, 0.4375, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.09375, 0.0625, 0.75, 0.15625, 0.4375, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.84375, 0.0625, 0.75, 0.90625, 0.4375, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.09375, 0.4375, 0.46875, 0.15625, 0.5, 0.53125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.84375, 0.4375, 0.46875, 0.90625, 0.5, 0.53125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.125, 0.5, 0.8125, 1.4375, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.09375, 0.59375, 0.46875, 0.15625, 1.4375, 0.53125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.84375, 0.59375, 0.46875, 0.90625, 1.4375, 0.53125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.84375, 1.46875, 0.46875, 0.90625, 1.53125, 0.53125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.09375, 1.46875, 0.46875, 0.15625, 1.53125, 0.53125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 1.46875, 0.46875, 0.8125, 1.53125, 0.53125), BooleanOp.OR);
        return shape.optimize();
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        // If slave redirect to master block if master log a message to chat for now
        if (!blockState.getValue(IS_SLAVE)) {
            player.sendSystemMessage(Component.literal("Master block clicked at " + blockPos.toShortString()));
            return InteractionResult.SUCCESS;
        }

        BlockPos masterPos = blockPos.below();
        BlockState masterState = level.getBlockState(masterPos);

        if (!(masterState.getBlock() instanceof PortalFrameBlock)) {
            player.sendSystemMessage(Component.literal("Master block not found at " + masterPos.toShortString()));
            return InteractionResult.FAIL;
        }

        return masterState.useWithoutItem(level, player, blockHitResult.withPosition(masterPos));
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        if (state.getValue(IS_SLAVE)) {
            return RenderShape.INVISIBLE;
        }
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.PORTAL_FRAME_BE.get().create(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        // If we are the master, create slave block above
        if (!blockState.getValue(IS_SLAVE)) {
            BlockPos slavePos = blockPos.above();
            BlockState slaveState = this.defaultBlockState().setValue(IS_SLAVE, true);
            level.setBlock(slavePos, slaveState, 3);
        }
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        // if we are slave remove master, if we are master remove slave
        if (blockState.getBlock() != blockState2.getBlock()) {
            if (blockState.getValue(IS_SLAVE)) {
                BlockPos masterPos = blockPos.below();
                BlockState masterState = level.getBlockState(masterPos);
                if (masterState.getBlock() instanceof PortalFrameBlock) {
                    level.removeBlock(masterPos, false);
                }
            } else {
                BlockPos slavePos = blockPos.above();
                BlockState slaveState = level.getBlockState(slavePos);
                if (slaveState.getBlock() instanceof PortalFrameBlock) {
                    level.removeBlock(slavePos, false);
                }
            }
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;
        if (blockEntityType.equals(BlockEntityRegistry.PORTAL_FRAME_BE.get())) return PortalFrameEntity::tick;
        return null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_SLAVE, FACING);
    }

    private void initializeShapes() {
        shapes.put(Direction.NORTH, baseShape);
        shapes.put(Direction.SOUTH, calculateRotation(Direction.SOUTH, baseShape));
        shapes.put(Direction.EAST, calculateRotation(Direction.EAST, baseShape));
        shapes.put(Direction.WEST, calculateRotation(Direction.WEST, baseShape));
    }

    @Override
    public @NotNull BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
