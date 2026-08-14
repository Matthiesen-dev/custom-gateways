package dev.matthiesen.custom_gateways.common.block.entity;

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

public final class PortalPadEntity extends AbstractLinkedBlockEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
        .thenLoop("animation.portal_pad.idle");

    public PortalPadEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.PORTAL_PAD_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(IDLE_ANIM)));
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (!(t instanceof PortalPadEntity portalPadEntity)) {
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

        portalPadEntity.setLinked(registry.getLinkedPortal(currentPortal) != null);
    }
}
