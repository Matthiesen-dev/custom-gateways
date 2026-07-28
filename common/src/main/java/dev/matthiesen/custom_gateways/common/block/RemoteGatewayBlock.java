package dev.matthiesen.custom_gateways.common.block;

import com.mojang.serialization.MapCodec;
import dev.matthiesen.custom_gateways.common.block.entity.RemoteGatewayBlockEntity;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RemoteGatewayBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty IS_TOP = BooleanProperty.create("is_top");

    public RemoteGatewayBlock() {
        super(BlockBehaviour.Properties.of()
            .noCollission()
            .strength(-1.0f, 3_600_000.0f)
            .noLootTable()
            .pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH).setValue(IS_TOP, false));
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, IS_TOP);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
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
}

