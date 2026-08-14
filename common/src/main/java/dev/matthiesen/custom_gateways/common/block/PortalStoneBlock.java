package dev.matthiesen.custom_gateways.common.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import dev.matthiesen.custom_gateways.common.block.entity.PortalStoneEntity;
import dev.matthiesen.custom_gateways.common.item.PortalLinkingDevice;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.util.Cleanup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class PortalStoneBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty IS_TOP = BooleanProperty.create("is_top");

    private static final double PARTICLE_Y_OFFSET = 0.88D;
    private static final double PARTICLE_SPREAD = 0.18D;
    private static final double PARTICLE_SPEED = 0.012D;
    private static final double PARTICLE_MAX_DISTANCE = 20.0D;

    private final Map<Direction, VoxelShape> lowerShapes = Maps.newEnumMap(Direction.class);
    private final Map<Direction, VoxelShape> upperShapes = Maps.newEnumMap(Direction.class);
    private final VoxelShape lowerBaseShape;
    private final VoxelShape upperBaseShape;

    public PortalStoneBlock() {
        super(
            BlockBehaviour.Properties.of()
                .noOcclusion()
                .strength(4f)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 5)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(IS_TOP, false));
        VoxelShape fullShape = createShape();
        this.lowerBaseShape = clipToLocalShape(fullShape, 0);
        this.upperBaseShape = clipToLocalShape(fullShape, 1);
        initializeShapes();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockPos topPos = pos.above();
        Level level = context.getLevel();
        if (level.isOutsideBuildHeight(topPos) || !level.getBlockState(topPos).canBeReplaced()) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (state.getValue(IS_TOP)) {
            return;
        }

        BlockState topState = this.defaultBlockState()
            .setValue(IS_TOP, true)
            .setValue(FACING, state.getValue(FACING));
        level.setBlock(pos.above(), topState, 3);
    }

    public BlockPos getMasterPos(Level level, BlockPos pos) {
        return getMasterPos(pos, level.getBlockState(pos));
    }

    public BlockPos getMasterPos(BlockPos pos, BlockState state) {
        return state.getValue(IS_TOP) ? pos.below() : pos;
    }

    public Block getParentBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PortalStoneBlock)) {
            return null;
        }
        return level.getBlockState(getMasterPos(pos, state)).getBlock();
    }

    public BlockState getParentBlockState(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PortalStoneBlock)) {
            return null;
        }
        return level.getBlockState(getMasterPos(pos, state));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, IS_TOP);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(IS_TOP)) {
            return upperShapes.getOrDefault(state.getValue(FACING), upperBaseShape);
        }
        return lowerShapes.getOrDefault(state.getValue(FACING), lowerBaseShape);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getShape(state, world, pos, context);
    }

    private VoxelShape createShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.3125, 0, 0.375, 0.6875, 0.125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.015625, 1.296875, 0.375, 0.796875, 1.484375, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.0625, 0.375, 0.6875, 1.375, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.0625, 0.375, 0.4375, 1.7875, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.125, 0.375, 0.625, 1.5, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.765625, 0.4375, 0.765625, 1.375, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.453125, 0.4375, 0.6875, 0.765625, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.453125, 0.4375, 0.4375, 0.765625, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.765625, 0.4375, 0.453125, 1.5625, 0.5625), BooleanOp.OR);
        return shape;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos masterPos = getMasterPos(pos, state);
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof PortalLinkingDevice) {
            return PortalLinkingDevice.useOnPortalEndpoint(level, player, masterPos);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(IS_TOP)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PortalStoneEntity portalStoneEntity) || !portalStoneEntity.isLinked()) {
            return;
        }

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + PARTICLE_Y_OFFSET;
        double centerZ = pos.getZ() + 0.5D;

        if (!level.hasNearbyAlivePlayer(centerX, centerY, centerZ, PARTICLE_MAX_DISTANCE)) {
            return;
        }

        if (random.nextFloat() < 0.385F) {
            double spawnX = centerX + randomBetween(random, -PARTICLE_SPREAD, PARTICLE_SPREAD);
            double spawnZ = centerZ + randomBetween(random, -PARTICLE_SPREAD, PARTICLE_SPREAD);
            double velocityX = randomBetween(random, -PARTICLE_SPEED, PARTICLE_SPEED);
            double velocityY = randomBetween(random, 0.006D, 0.018D);
            double velocityZ = randomBetween(random, -PARTICLE_SPEED, PARTICLE_SPEED);
            level.addParticle(ParticleTypes.PORTAL, spawnX, centerY, spawnZ, velocityX, velocityY, velocityZ);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState nextState, boolean isMoving) {
        if (state.getBlock() != nextState.getBlock()) {
            BlockPos masterPos = getMasterPos(pos, state);
            BlockPos counterpartPos = state.getValue(IS_TOP) ? pos.below() : pos.above();
            BlockState counterpart = level.getBlockState(counterpartPos);
            if (counterpart.is(this) && counterpart.getValue(IS_TOP) != state.getValue(IS_TOP)) {
                level.removeBlock(counterpartPos, false);
            }

            if (!level.isClientSide && !state.getValue(IS_TOP) && level instanceof ServerLevel serverLevel) {
                Cleanup.portalLinks(serverLevel, masterPos);
            }
        }

        super.onRemove(state, level, pos, nextState, isMoving);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return state.getValue(IS_TOP) ? RenderShape.INVISIBLE : RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(IS_TOP)) {
            return null;
        }
        return BlockEntityRegistry.PORTAL_STONE_BE.get().create(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || state.getValue(IS_TOP)) {
            return null;
        }
        if (type == BlockEntityRegistry.PORTAL_STONE_BE.get()) {
            return PortalStoneEntity::tick;
        }
        return null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    private void initializeShapes() {
        lowerShapes.put(Direction.NORTH, lowerBaseShape);
        lowerShapes.put(Direction.SOUTH, calculateRotation(Direction.SOUTH, lowerBaseShape));
        lowerShapes.put(Direction.EAST, calculateRotation(Direction.EAST, lowerBaseShape));
        lowerShapes.put(Direction.WEST, calculateRotation(Direction.WEST, lowerBaseShape));

        upperShapes.put(Direction.NORTH, upperBaseShape);
        upperShapes.put(Direction.SOUTH, calculateRotation(Direction.SOUTH, upperBaseShape));
        upperShapes.put(Direction.EAST, calculateRotation(Direction.EAST, upperBaseShape));
        upperShapes.put(Direction.WEST, calculateRotation(Direction.WEST, upperBaseShape));
    }

    private static VoxelShape clipToLocalShape(VoxelShape sourceShape, int yOffset) {
        VoxelShape[] clippedShape = new VoxelShape[]{Shapes.empty()};
        sourceShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double shiftedMinY = minY - yOffset;
            double shiftedMaxY = maxY - yOffset;

            double clampedMinX = Math.max(0.0D, minX);
            double clampedMinY = Math.max(0.0D, shiftedMinY);
            double clampedMinZ = Math.max(0.0D, minZ);
            double clampedMaxX = Math.min(1.0D, maxX);
            double clampedMaxY = Math.min(1.0D, shiftedMaxY);
            double clampedMaxZ = Math.min(1.0D, maxZ);

            if (clampedMinX < clampedMaxX && clampedMinY < clampedMaxY && clampedMinZ < clampedMaxZ) {
                VoxelShape box = Shapes.box(clampedMinX, clampedMinY, clampedMinZ, clampedMaxX, clampedMaxY, clampedMaxZ);
                clippedShape[0] = Shapes.joinUnoptimized(clippedShape[0], box, BooleanOp.OR);
            }
        });
        return clippedShape[0].optimize();
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

    private static double randomBetween(RandomSource random, double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}

