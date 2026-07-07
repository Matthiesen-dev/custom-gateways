package dev.matthiesen.custom_gateways.common.block;

import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PortalFrameBlock extends Block implements EntityBlock {

    public PortalFrameBlock() {
        super(Properties.of().noOcclusion());
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.PORTAL_FRAME_BE.get().create(blockPos, blockState);
    }
}
