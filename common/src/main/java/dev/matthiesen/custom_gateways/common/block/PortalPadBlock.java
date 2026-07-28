package dev.matthiesen.custom_gateways.common.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.block.entity.PortalPadEntity;
import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import dev.matthiesen.custom_gateways.common.item.PortalLinkingCard;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class PortalPadBlock extends HorizontalDirectionalBlock implements EntityBlock {
    private static final double PARTICLE_Y_OFFSET = 0.12D;
    private static final double PARTICLE_SPREAD = 0.24D;
    private static final double PARTICLE_SPEED = 0.015D;
    private static final double PARTICLE_MAX_DISTANCE = 20.0D;

    private final Map<Direction, VoxelShape> shapes = Maps.newEnumMap(Direction.class);
    private final VoxelShape baseShape;

    public PortalPadBlock() {
        super(BlockBehaviour.Properties.of().noOcclusion()
                .strength(4f)
                .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.baseShape = createShape();
        initializeShapes();
    }

    @Override
    public @NotNull BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return shapes.getOrDefault(state.getValue(FACING), baseShape);
    }

    private VoxelShape createShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-0.125, 0, -0.125, 1.125, 0.0625, 1.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.0625, 0, 1, 0.125, 1), BooleanOp.OR);
        return shape;
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

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof PortalLinkingCard) {
            return PortalLinkingCard.useOnPortalEndpoint(level, player, blockPos);
        }
        // Let other items process use-on behavior (e.g. Remote Dialer saving destinations).
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PortalPadEntity portalPadEntity) || !portalPadEntity.isLinked()) {
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
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.getBlock() != blockState2.getBlock() && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            PortalRegistry registry = PortalRegistry.get(serverLevel);
            PortalRegistry.PortalLocation portalLocation =
                new PortalRegistry.PortalLocation(level.dimension().location(), blockPos);

            PortalRegistry.PortalLocation linkedLocation = registry.getLinkedPortal(portalLocation);
            registry.removePortal(portalLocation);

            if (linkedLocation != null) {
                Level linkedLevel = resolveLevel(serverLevel, linkedLocation.dimension());
                BlockEntity linkedEntity = linkedLevel.getBlockEntity(linkedLocation.getBlockPos());
                if (linkedEntity instanceof PortalFrameEntity linkedPortalEntity) {
                    linkedPortalEntity.clearLinkedTarget();
                } else if (linkedEntity instanceof PortalPadEntity linkedPortalPadEntity) {
                    linkedPortalPadEntity.setLinked(false);
                }
            }
        }

        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.PORTAL_PAD_BE.get().create(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        if (blockEntityType.equals(BlockEntityRegistry.PORTAL_PAD_BE.get())) {
            return PortalPadEntity::tick;
        }
        return null;
    }

    private static Level resolveLevel(ServerLevel currentLevel, ResourceLocation dimension) {
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimension);
        ServerLevel resolved = currentLevel.getServer().getLevel(dimensionKey);
        return resolved != null ? resolved : currentLevel;
    }

    private static double randomBetween(RandomSource random, double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }
}

