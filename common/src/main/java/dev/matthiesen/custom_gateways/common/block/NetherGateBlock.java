package dev.matthiesen.custom_gateways.common.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import dev.matthiesen.custom_gateways.common.block.entity.NetherGateEntity;
import dev.matthiesen.custom_gateways.common.item.PortalLinkingDevice;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.util.Cleanup;
import dev.matthiesen.custom_gateways.common.util.VoxelShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class NetherGateBlock extends HorizontalDirectionalBlock implements EntityBlock {
    private static final double PARTICLE_Y_OFFSET = 0.13D;
    private static final double PARTICLE_SPREAD = 0.55D;
    private static final double PARTICLE_SPEED = 0.015D;
    private static final double PARTICLE_MAX_DISTANCE = 24.0D;

    private final Map<Direction, VoxelShape> shapes = Maps.newEnumMap(Direction.class);
    private final VoxelShape baseShape;

    public NetherGateBlock() {
        super(
            BlockBehaviour.Properties.of()
                .noOcclusion()
                .strength(4f)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 7)
        );
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
        // Main flat plate, slightly wider than the block to match the geo's protruding rim
        shape = Shapes.join(shape, Shapes.box(-0.0625, 0, -0.0625, 1.0625, 0.0625, 1.0625), BooleanOp.OR);
        // Thin central surface the player stands on
        shape = Shapes.join(shape, Shapes.box(0, 0.0625, 0, 1, 0.125, 1), BooleanOp.OR);
        return shape;
    }

    private void initializeShapes() {
        shapes.put(Direction.NORTH, baseShape);
        shapes.put(Direction.SOUTH, VoxelShapeUtil.calculateRotation(Direction.SOUTH, baseShape));
        shapes.put(Direction.EAST, VoxelShapeUtil.calculateRotation(Direction.EAST, baseShape));
        shapes.put(Direction.WEST, VoxelShapeUtil.calculateRotation(Direction.WEST, baseShape));
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof PortalLinkingDevice) {
            return PortalLinkingDevice.useOnPortalEndpoint(level, player, blockPos);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof NetherGateEntity netherGateEntity) || !netherGateEntity.isLinked()) {
            return;
        }

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + PARTICLE_Y_OFFSET;
        double centerZ = pos.getZ() + 0.5D;

        if (!level.hasNearbyAlivePlayer(centerX, centerY, centerZ, PARTICLE_MAX_DISTANCE)) {
            return;
        }

        if (random.nextFloat() < 0.55F) {
            double angle = random.nextDouble() * 2.0D * Math.PI;
            double radius = VoxelShapeUtil.randomBetween(random, 0.08D, PARTICLE_SPREAD);
            double spawnX = centerX + Math.cos(angle) * radius;
            double spawnZ = centerZ + Math.sin(angle) * radius;
            double velocityX = VoxelShapeUtil.randomBetween(random, -PARTICLE_SPEED, PARTICLE_SPEED);
            double velocityY = VoxelShapeUtil.randomBetween(random, 0.003D, 0.018D);
            double velocityZ = VoxelShapeUtil.randomBetween(random, -PARTICLE_SPEED, PARTICLE_SPEED);
            level.addParticle(
                random.nextFloat() < 0.3F ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                spawnX, centerY, spawnZ, velocityX, velocityY, velocityZ
            );
        }
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.getBlock() != blockState2.getBlock() && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            Cleanup.portalLinks(serverLevel, blockPos);
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.NETHER_GATE_BE.get().create(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        if (blockEntityType.equals(BlockEntityRegistry.NETHER_GATE_BE.get())) {
            return NetherGateEntity::tick;
        }
        return null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }
}

