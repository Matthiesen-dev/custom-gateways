package dev.matthiesen.custom_gateways.common.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class PortalFrameBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty IS_SLAVE = BooleanProperty.create("is_slave");
    private static final double PARTICLE_MAX_DISTANCE = 28.0D;
    private static final int MAX_PARTICLES_PER_TICK = 4;
    private static final double TARGET_Y_OFFSET = 1.0D;
    private static final double OPENING_HALF_WIDTH = 0.42D;
    private static final double OPENING_MIN_Y = 0.15D;
    private static final double OPENING_MAX_Y = 1.95D;
    private static final double EDGE_MIN_LATERAL = 0.50D;
    private static final double EDGE_MAX_LATERAL = 0.68D;
    private static final double TOP_MIN_Y = 1.95D;
    private static final double TOP_MAX_Y = 2.22D;
    private static final double BOTTOM_MIN_Y = 0.02D;
    private static final double BOTTOM_MAX_Y = 0.22D;
    private static final double EDGE_DEPTH_VARIANCE = 0.18D;
    private static final double CENTER_DEPTH_VARIANCE = 0.10D;
    private static final float EDGE_SPAWN_CHANCE = 0.65F;

    private final Map<Direction, VoxelShape> shapes = Maps.newEnumMap(Direction.class);
    private final Map<Direction, VoxelShape> slaveShapes = Maps.newEnumMap(Direction.class);
    private final VoxelShape baseShape;
    private final VoxelShape slaveBaseShape;
    private final VoxelShape collisionShape;

    public PortalFrameBlock() {
        super(
                BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .strength(4f)
                        .requiresCorrectToolForDrops()
                        .lightLevel(state -> 7)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(IS_SLAVE, false).setValue(FACING, Direction.NORTH));
        this.baseShape = createShape();
        this.slaveBaseShape = createSlaveShape();
        this.collisionShape = createCollisionShape();
        initializeShapes();
        initializeSlaveShapes();
    }

    private VoxelShape createShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-0.19562500000000005, 0, -0.19562500000000005, 1.1956250000000002, 0.099375, 1.1956250000000002), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.003124999999999989, 0.099375, 0.003124999999999989, 0.996875, 0.19875, 0.996875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.996875, 0.099375, 0.400625, 1.1956250000000002, 0.5962500000000001, 0.599375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.19562500000000005, 0.099375, 0.400625, 0.003124999999999989, 0.5962500000000001, 0.599375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.0465625, 0.099375, 0.003124999999999989, 1.1459375, 0.695625, 0.10249999999999998), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.14593750000000005, 0.099375, 0.003124999999999989, -0.04656250000000006, 0.695625, 0.10249999999999998), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.14593750000000005, 0.099375, 0.8975, -0.04656250000000006, 0.695625, 0.996875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.0465625, 0.099375, 0.8975, 1.1459375, 0.695625, 0.996875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.14593750000000005, 0.695625, 0.4503125, -0.04656250000000006, 0.795, 0.5496875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.0465625, 0.695625, 0.4503125, 1.1459375, 0.795, 0.5496875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.003124999999999989, 0.19875, 0.5, 0.996875, 2.285625, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.14593750000000005, 0.9440625, 0.4503125, -0.04656250000000006, 2.285625, 0.5496875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.0465625, 0.9440625, 0.4503125, 1.1459375, 2.285625, 0.5496875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.0465625, 2.3353125, 0.4503125, 1.1459375, 2.4346875000000003, 0.5496875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.14593750000000005, 2.3353125, 0.4503125, -0.04656250000000006, 2.4346875000000003, 0.5496875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.003124999999999989, 2.3353125, 0.4503125, 0.996875, 2.4346875000000003, 0.5496875), BooleanOp.OR);
        return shape;
    }

    private VoxelShape createCollisionShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-0.19562500000000005, 0, -0.19562500000000005, 1.1956250000000002, 0.099375, 1.1956250000000002), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.003124999999999989, 0.099375, 0.003124999999999989, 0.996875, 0.19875, 0.996875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.996875, 0.099375, 0.400625, 1.1956250000000002, 0.5962500000000001, 0.599375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.19562500000000005, 0.099375, 0.400625, 0.003124999999999989, 0.5962500000000001, 0.599375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.0465625, 0.099375, 0.003124999999999989, 1.1459375, 0.695625, 0.10249999999999998), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.14593750000000005, 0.099375, 0.003124999999999989, -0.04656250000000006, 0.695625, 0.10249999999999998), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.14593750000000005, 0.099375, 0.8975, -0.04656250000000006, 0.695625, 0.996875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.0465625, 0.099375, 0.8975, 1.1459375, 0.695625, 0.996875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.14593750000000005, 0.695625, 0.4503125, -0.04656250000000006, 0.795, 0.5496875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.0465625, 0.695625, 0.4503125, 1.1459375, 0.795, 0.5496875), BooleanOp.OR);
        return shape;
    }

    /**
     * Creates the outline/interaction shape for the slave (upper) block.
     * This represents the upper portion of the portal frame in slave-local coordinates
     * (master Y > 1.0, shifted down by 1.0 to slave-local Y = 0.0 origin).
     */
    private VoxelShape createSlaveShape() {
        VoxelShape shape = Shapes.empty();
        // Left arch post – slave portion (master Y 0.944→2.286, clamped to 1.0→2.286 → slave Y 0→1.286)
        shape = Shapes.join(shape, Shapes.box(-0.14593750000000005, 0.0, 0.4503125, -0.04656250000000006, 1.285625, 0.5496875), BooleanOp.OR);
        // Right arch post – slave portion
        shape = Shapes.join(shape, Shapes.box(1.0465625, 0.0, 0.4503125, 1.1459375, 1.285625, 0.5496875), BooleanOp.OR);
        // Left top cap  (master Y 2.335→2.435 → slave Y 1.335→1.435)
        shape = Shapes.join(shape, Shapes.box(-0.14593750000000005, 1.3353125, 0.4503125, -0.04656250000000006, 1.4346875000000003, 0.5496875), BooleanOp.OR);
        // Right top cap
        shape = Shapes.join(shape, Shapes.box(1.0465625, 1.3353125, 0.4503125, 1.1459375, 1.4346875000000003, 0.5496875), BooleanOp.OR);
        // Top centre bar
        shape = Shapes.join(shape, Shapes.box(0.003124999999999989, 1.3353125, 0.4503125, 0.996875, 1.4346875000000003, 0.5496875), BooleanOp.OR);
        return shape;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        // If slave redirect to master block
        if (blockState.getValue(IS_SLAVE)) {
            BlockPos masterPos = blockPos.below();
            BlockState masterState = level.getBlockState(masterPos);

            if (!(masterState.getBlock() instanceof PortalFrameBlock)) {
                player.sendSystemMessage(Component.translatable("interaction.custom_gateways.portal_frame.error.master_block", masterPos.toShortString()));
                return InteractionResult.FAIL;
            }

            return masterState.useWithoutItem(level, player, blockHitResult.withPosition(masterPos));
        }

        // Check if player is holding a linking card
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof PortalLinkingDevice) {
            return PortalLinkingDevice.useOnPortalEndpoint(level, player, blockPos);
        }

        // Let other items process use-on behavior (e.g. Remote Dialer saving destinations).
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        if (state.getValue(IS_SLAVE)) {
            return RenderShape.INVISIBLE;
        }
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public Block getParentBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof PortalFrameBlock) {
            if (state.getValue(IS_SLAVE)) {
                return level.getBlockState(pos.below()).getBlock();
            } else {
                return state.getBlock();
            }
        }
        return null;
    }

    public BlockState getParentBlockState(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof PortalFrameBlock) {
            if (state.getValue(IS_SLAVE)) {
                return level.getBlockState(pos.below());
            } else {
                return state;
            }
        }
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.PORTAL_FRAME_BE.get().create(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        // If we are the master, create slave block above – inherit FACING so the slave's shape is correctly oriented
        if (!blockState.getValue(IS_SLAVE)) {
            BlockPos slavePos = blockPos.above();
            BlockState slaveState = this.defaultBlockState()
                    .setValue(IS_SLAVE, true)
                    .setValue(FACING, blockState.getValue(FACING));
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

                // Clean up portal links if this is the master block being removed
                if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                    Cleanup.portalLinks(serverLevel, blockPos);
                }
            }
        }

        super.onRemove(blockState, level, blockPos, blockState2, bl);
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

    private void initializeSlaveShapes() {
        slaveShapes.put(Direction.NORTH, slaveBaseShape);
        slaveShapes.put(Direction.SOUTH, calculateRotation(Direction.SOUTH, slaveBaseShape));
        slaveShapes.put(Direction.EAST, calculateRotation(Direction.EAST, slaveBaseShape));
        slaveShapes.put(Direction.WEST, calculateRotation(Direction.WEST, slaveBaseShape));
    }

    @Override
    public @NotNull BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(IS_SLAVE)) {
            return slaveShapes.getOrDefault(state.getValue(FACING), slaveBaseShape);
        }
        return shapes.getOrDefault(state.getValue(FACING), baseShape);
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(IS_SLAVE)) {
            return Shapes.empty();
        }
        return collisionShape;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(IS_SLAVE)) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PortalFrameEntity portalFrameEntity) || !portalFrameEntity.isLinked()) {
            return;
        }

        double targetX = pos.getX() + 0.5D;
        double targetY = pos.getY() + TARGET_Y_OFFSET;
        double targetZ = pos.getZ() + 0.5D;

        double maxDistanceSqr = PARTICLE_MAX_DISTANCE * PARTICLE_MAX_DISTANCE;
        if (!portalFrameEntity.hasNearbyPlayerCached(level, targetX, targetY, targetZ, maxDistanceSqr)) {
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
                    // Side pillars around the gateway opening.
                    double side = random.nextBoolean() ? 1.0D : -1.0D;
                    lateral = side * randomBetween(random, EDGE_MIN_LATERAL, EDGE_MAX_LATERAL);
                    height = randomBetween(random, OPENING_MIN_Y, TOP_MAX_Y);
                } else {
                    // Top/bottom frame bars to make the effect surround the gateway.
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
