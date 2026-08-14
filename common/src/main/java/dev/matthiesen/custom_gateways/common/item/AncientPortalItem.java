package dev.matthiesen.custom_gateways.common.item;

import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import software.bernie.geckolib.animation.RawAnimation;

public final class AncientPortalItem extends AbstractTransporter implements DimensionalGateItem {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
        .thenLoop("animation.ancient_portal.linked");

    public AncientPortalItem() {
        super(BlockRegistry.ANCIENT_PORTAL.get());
    }

    @Override
    public RawAnimation getIdleAnimation() {
        return IDLE_ANIM;
    }
}

