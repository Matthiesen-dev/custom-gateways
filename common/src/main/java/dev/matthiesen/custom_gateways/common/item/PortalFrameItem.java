package dev.matthiesen.custom_gateways.common.item;

import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import software.bernie.geckolib.animation.RawAnimation;

public final class PortalFrameItem extends AbstractTransporter implements DimensionalGateItem {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
            .thenLoop("animation.portal_frame.linked");

    public PortalFrameItem() {
        super(BlockRegistry.PORTAL_FRAME.get());
    }

    @Override
    public RawAnimation getIdleAnimation() {
        return IDLE_ANIM;
    }
}
