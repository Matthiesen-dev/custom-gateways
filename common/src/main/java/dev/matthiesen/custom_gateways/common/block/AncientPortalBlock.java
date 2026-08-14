package dev.matthiesen.custom_gateways.common.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import dev.matthiesen.custom_gateways.common.block.entity.AncientPortalEntity;
import dev.matthiesen.custom_gateways.common.item.PortalLinkingDevice;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.util.Cleanup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class AncientPortalBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final IntegerProperty PART_X = IntegerProperty.create("part_x", 0, 2);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, 2);

    private static final int MASTER_PART_X = 1;
    private static final int MASTER_PART_Y = 0;
    private static final double PARTICLE_MAX_DISTANCE = 32.0D;
    private static final int MAX_PARTICLES_PER_TICK = 6;
    private static final double TARGET_Y_OFFSET = 1.3D;
    private static final double OPENING_HALF_WIDTH = 0.9D;
    private static final double OPENING_MIN_Y = 0.18D;
    private static final double OPENING_MAX_Y = 2.30D;
    private static final double EDGE_MIN_LATERAL = 1.14D;
    private static final double EDGE_MAX_LATERAL = 1.48D;
    private static final double TOP_MIN_Y = 2.15D;
    private static final double TOP_MAX_Y = 2.58D;
    private static final double BOTTOM_MIN_Y = 0.01D;
    private static final double BOTTOM_MAX_Y = 0.18D;
    private static final double EDGE_DEPTH_VARIANCE = 0.12D;
    private static final double CENTER_DEPTH_VARIANCE = 0.07D;
    private static final float EDGE_SPAWN_CHANCE = 0.72F;

    private final Map<Direction, VoxelShape> masterShapes = Maps.newEnumMap(Direction.class);
    private final Map<Direction, VoxelShape[]> partShapes = Maps.newEnumMap(Direction.class);
    private final VoxelShape baseShape;

    public AncientPortalBlock() {
        super(
            BlockBehaviour.Properties.of()
                .noOcclusion()
                .strength(4f)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 7)
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(PART_X, MASTER_PART_X)
            .setValue(PART_Y, MASTER_PART_Y));
        this.baseShape = createShape();
        initializeShapes();
    }

    private VoxelShape createShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(1.6875, 0, 0.25, 2.1875, 0.25, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.6875, 0.25, 0.25, 2.1875, 0.5, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.6875, 0.5, 0.25, 2.1875, 0.75, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.6875, 0.75, 0.25, 2.1875, 1, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.6875, 1, 0.25, 2.1875, 1.25, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 0, 0.25, -0.6875, 0.25, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 0.25, 0.25, -0.6875, 0.5, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 0.5, 0.25, -0.6875, 0.75, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 0.75, 0.25, -0.6875, 1, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 1, 0.25, -0.6875, 1.25, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 2.125, 0.25, -0.6875, 2.625, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 1.75, 0.25, -0.6875, 2, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 1.5, 0.25, -0.6875, 1.75, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 1.25, 0.25, -0.6875, 1.5, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.6875, 2.125, 0.25, 2.1875, 2.625, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.6875, 1.75, 0.25, 2.1875, 2, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.6875, 1.5, 0.25, 2.1875, 1.75, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.6875, 1.25, 0.25, 2.1875, 1.5, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.5, 0.125, 0.5, 1.5, 2.4375, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.5625, 0, 0.4375, 1.5625, 0.03125, 0.5625), BooleanOp.OR);
        return shape;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        BlockPos masterPos = getMasterPos(blockPos, blockState);
        if (!masterPos.equals(blockPos)) {
            BlockState masterState = level.getBlockState(masterPos);
            if (!(masterState.getBlock() instanceof AncientPortalBlock)) {
                player.sendSystemMessage(Component.translatable("interaction.custom_gateways.ancient_portal.error.master_block", masterPos.toShortString()));
                return InteractionResult.FAIL;
            }
            return masterState.useWithoutItem(level, player, blockHitResult.withPosition(masterPos));
        }

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof PortalLinkingDevice) {
            return PortalLinkingDevice.useOnPortalEndpoint(level, player, masterPos);
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return isMaster(state) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
    }

    public boolean isMaster(BlockState state) {
        return state.getValue(PART_X) == MASTER_PART_X && state.getValue(PART_Y) == MASTER_PART_Y;
    }

    public BlockPos getMasterPos(Level level, BlockPos pos) {
        return getMasterPos(pos, level.getBlockState(pos));
    }

    public BlockPos getMasterPos(BlockPos pos, BlockState state) {
        int lateralOffset = state.getValue(PART_X) - MASTER_PART_X;
        int verticalOffset = state.getValue(PART_Y) - MASTER_PART_Y;
        Direction lateralDirection = state.getValue(FACING).getClockWise();
        return moveLaterally(pos, lateralDirection, -lateralOffset).below(verticalOffset);
    }

    public Block getParentBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof AncientPortalBlock)) {
            return null;
        }
        return level.getBlockState(getMasterPos(pos, state)).getBlock();
    }

    public BlockState getParentBlockState(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof AncientPortalBlock)) {
            return null;
        }
        return level.getBlockState(getMasterPos(pos, state));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        if (!isMaster(blockState)) {
            return null;
        }
        return BlockEntityRegistry.ANCIENT_PORTAL_BE.get().create(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        if (!isMaster(blockState)) {
            return;
        }

        Direction facing = blockState.getValue(FACING);
        for (int partY = 0; partY < 3; partY++) {
            for (int partX = 0; partX < 3; partX++) {
                if (partX == MASTER_PART_X && partY == MASTER_PART_Y) {
                    continue;
                }

                BlockPos partPos = getPartPos(blockPos, facing, partX, partY);
                BlockState partState = this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART_X, partX)
                    .setValue(PART_Y, partY);
                level.setBlock(partPos, partState, 3);
            }
        }
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState replacementState, boolean movedByPiston) {
        if (blockState.getBlock() != replacementState.getBlock()) {
            BlockPos masterPos = getMasterPos(blockPos, blockState);
            if (!isMaster(blockState)) {
                BlockState masterState = level.getBlockState(masterPos);
                if (masterState.getBlock() instanceof AncientPortalBlock) {
                    level.removeBlock(masterPos, false);
                }
            } else {
                Direction facing = blockState.getValue(FACING);
                for (int partY = 0; partY < 3; partY++) {
                    for (int partX = 0; partX < 3; partX++) {
                        if (partX == MASTER_PART_X && partY == MASTER_PART_Y) {
                            continue;
                        }

                        BlockPos partPos = getPartPos(blockPos, facing, partX, partY);
                        BlockState partState = level.getBlockState(partPos);
                        if (partState.getBlock() instanceof AncientPortalBlock) {
                            level.removeBlock(partPos, false);
                        }
                    }
                }

                if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                    Cleanup.portalLinks(serverLevel, blockPos);
                }
            }
        }

        super.onRemove(blockState, level, blockPos, replacementState, movedByPiston);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        if (blockEntityType.equals(BlockEntityRegistry.ANCIENT_PORTAL_BE.get())) {
            return AncientPortalEntity::tick;
        }
        return null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART_X, PART_Y);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos masterPos = context.getClickedPos();
        Level level = context.getLevel();

        for (int partY = 0; partY < 3; partY++) {
            for (int partX = 0; partX < 3; partX++) {
                BlockPos partPos = getPartPos(masterPos, facing, partX, partY);
                if (level.isOutsideBuildHeight(partPos) || !level.getBlockState(partPos).canBeReplaced()) {
                    return null;
                }
            }
        }

        return this.defaultBlockState()
            .setValue(FACING, facing)
            .setValue(PART_X, MASTER_PART_X)
            .setValue(PART_Y, MASTER_PART_Y);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (isMaster(state)) {
            return masterShapes.getOrDefault(facing, baseShape);
        }
        return getPartShape(facing, state.getValue(PART_X), state.getValue(PART_Y));
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return getPartShape(blockState.getValue(FACING), blockState.getValue(PART_X), blockState.getValue(PART_Y));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!isMaster(state)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AncientPortalEntity ancientPortalEntity) || !ancientPortalEntity.isLinked()) {
            return;
        }

        double targetX = pos.getX() + 0.5D;
        double targetY = pos.getY() + TARGET_Y_OFFSET;
        double targetZ = pos.getZ() + 0.5D;

        double maxDistanceSqr = PARTICLE_MAX_DISTANCE * PARTICLE_MAX_DISTANCE;
        if (!ancientPortalEntity.hasNearbyPlayerCached(level, targetX, targetY, targetZ, maxDistanceSqr)) {
            return;
        }

        Direction facing = state.getValue(FACING);
        Vec3 normal = new Vec3(facing.getStepX(), 0.0D, facing.getStepZ());
        Vec3 lateralAxis = new Vec3(facing.getClockWise().getStepX(), 0.0D, facing.getClockWise().getStepZ());

        int particleCount = 1 + random.nextInt(MAX_PARTICLES_PER_TICK);
        for (int i = 0; i < particleCount; i++) {
            double lateral;
            double height;
            double depth;

            if (random.nextFloat() < EDGE_SPAWN_CHANCE) {
                depth = randomBetween(random, -EDGE_DEPTH_VARIANCE, EDGE_DEPTH_VARIANCE);

                if (random.nextBoolean()) {
                    double side = random.nextBoolean() ? 1.0D : -1.0D;
                    lateral = side * randomBetween(random, EDGE_MIN_LATERAL, EDGE_MAX_LATERAL);
                    height = randomBetween(random, OPENING_MIN_Y, TOP_MAX_Y);
                } else {
                    lateral = randomBetween(random, -EDGE_MAX_LATERAL, EDGE_MAX_LATERAL);
                    if (random.nextBoolean()) {
                        height = randomBetween(random, TOP_MIN_Y, TOP_MAX_Y);
                    } else {
                        height = randomBetween(random, BOTTOM_MIN_Y, BOTTOM_MAX_Y);
                    }
                }
            } else {
                lateral = randomBetween(random, -OPENING_HALF_WIDTH, OPENING_HALF_WIDTH);
                height = randomBetween(random, OPENING_MIN_Y, OPENING_MAX_Y);
                depth = randomBetween(random, -CENTER_DEPTH_VARIANCE, CENTER_DEPTH_VARIANCE);
            }

            double spawnX = targetX + lateralAxis.x * lateral + normal.x * depth;
            double spawnY = pos.getY() + height;
            double spawnZ = targetZ + lateralAxis.z * lateral + normal.z * depth;

            double dx = targetX - spawnX;
            double dy = targetY - spawnY;
            double dz = targetZ - spawnZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < 1.0E-4D) {
                continue;
            }

            double speed = randomBetween(random, 0.015D, 0.035D);
            double jitter = 0.004D;
            double velocityX = dx / distance * speed + randomBetween(random, -jitter, jitter);
            double velocityY = dy / distance * speed + randomBetween(random, -jitter, jitter);
            double velocityZ = dz / distance * speed + randomBetween(random, -jitter, jitter);

            level.addParticle(pickGatewayParticle(random), spawnX, spawnY, spawnZ, velocityX, velocityY, velocityZ);
        }
    }

    private void initializeShapes() {
        masterShapes.put(Direction.NORTH, baseShape);
        masterShapes.put(Direction.SOUTH, calculateRotation(Direction.SOUTH, baseShape));
        masterShapes.put(Direction.EAST, calculateRotation(Direction.EAST, baseShape));
        masterShapes.put(Direction.WEST, calculateRotation(Direction.WEST, baseShape));

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            VoxelShape[] shapes = new VoxelShape[9];
            VoxelShape masterShape = masterShapes.get(direction);
            for (int partY = 0; partY < 3; partY++) {
                for (int partX = 0; partX < 3; partX++) {
                    BlockPos offset = getPartOffset(direction, partX, partY);
                    shapes[toIndex(partX, partY)] = clipToLocalShape(masterShape, offset.getX(), offset.getY(), offset.getZ());
                }
            }
            partShapes.put(direction, shapes);
        }
    }

    private VoxelShape getPartShape(Direction facing, int partX, int partY) {
        VoxelShape[] shapes = partShapes.get(facing);
        if (shapes == null) {
            return Shapes.empty();
        }
        return shapes[toIndex(partX, partY)];
    }

    private static int toIndex(int partX, int partY) {
        return partY * 3 + partX;
    }

    private static BlockPos getPartPos(BlockPos masterPos, Direction facing, int partX, int partY) {
        BlockPos offset = getPartOffset(facing, partX, partY);
        return masterPos.offset(offset);
    }

    private static BlockPos getPartOffset(Direction facing, int partX, int partY) {
        int lateralOffset = partX - MASTER_PART_X;
        Direction lateralDirection = facing.getClockWise();
        return new BlockPos(lateralDirection.getStepX() * lateralOffset, partY, lateralDirection.getStepZ() * lateralOffset);
    }

    private static BlockPos moveLaterally(BlockPos pos, Direction lateralDirection, int offset) {
        if (offset == 0) {
            return pos;
        }
        if (offset > 0) {
            return pos.relative(lateralDirection, offset);
        }
        return pos.relative(lateralDirection.getOpposite(), -offset);
    }

    private static VoxelShape clipToLocalShape(VoxelShape sourceShape, int offsetX, int offsetY, int offsetZ) {
        VoxelShape[] clippedShape = new VoxelShape[]{Shapes.empty()};
        sourceShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double shiftedMinX = minX - offsetX;
            double shiftedMinY = minY - offsetY;
            double shiftedMinZ = minZ - offsetZ;
            double shiftedMaxX = maxX - offsetX;
            double shiftedMaxY = maxY - offsetY;
            double shiftedMaxZ = maxZ - offsetZ;

            double clampedMinX = Math.max(0.0D, shiftedMinX);
            double clampedMinY = Math.max(0.0D, shiftedMinY);
            double clampedMinZ = Math.max(0.0D, shiftedMinZ);
            double clampedMaxX = Math.min(1.0D, shiftedMaxX);
            double clampedMaxY = Math.min(1.0D, shiftedMaxY);
            double clampedMaxZ = Math.min(1.0D, shiftedMaxZ);

            if (clampedMinX < clampedMaxX && clampedMinY < clampedMaxY && clampedMinZ < clampedMaxZ) {
                VoxelShape box = Shapes.box(clampedMinX, clampedMinY, clampedMinZ, clampedMaxX, clampedMaxY, clampedMaxZ);
                clippedShape[0] = Shapes.joinUnoptimized(clippedShape[0], box, BooleanOp.OR);
            }
        });
        return clippedShape[0].optimize();
    }

    private static ParticleOptions pickGatewayParticle(RandomSource random) {
        if (random.nextFloat() < 0.4F) {
            return ParticleTypes.ENCHANT;
        }
        return ParticleTypes.PORTAL;
    }

    private static double randomBetween(RandomSource random, double min, double max) {
        return min + (max - min) * random.nextDouble();
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



