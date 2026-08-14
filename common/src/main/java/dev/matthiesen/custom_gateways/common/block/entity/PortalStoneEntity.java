package dev.matthiesen.custom_gateways.common.block.entity;

import dev.matthiesen.custom_gateways.common.block.PortalStoneBlock;
import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public final class PortalStoneEntity extends AbstractLinkedBlockEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
        .thenLoop("animation.portal_stone.idle");

    public PortalStoneEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.PORTAL_STONE_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(IDLE_ANIM)));
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (!(blockEntity instanceof PortalStoneEntity portalStoneEntity)) {
            return;
        }
        if (blockState.getValue(PortalStoneBlock.IS_TOP)) {
            return;
        }
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (serverLevel.getGameTime() % LINK_STATE_CHECK_INTERVAL_TICKS != 0L) {
            return;
        }

        PortalRegistry registry = PortalRegistry.get(serverLevel);
        PortalRegistry.PortalLocation currentPortal =
            new PortalRegistry.PortalLocation(level.dimension().location(), blockPos);

        portalStoneEntity.setLinked(registry.getLinkedPortal(currentPortal) != null);
    }
}
