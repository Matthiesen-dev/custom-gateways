package dev.matthiesen.custom_gateways.common.item;

import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import software.bernie.geckolib.animation.RawAnimation;

public final class NetherGateItem extends AbstractTransporter implements DimensionalGateItem {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
        .thenLoop("animation.nether_gate.linked");

    public NetherGateItem() {
        super(BlockRegistry.NETHER_GATE.get());
    }

    @Override
    public RawAnimation getIdleAnimation() {
        return IDLE_ANIM;
    }
}

