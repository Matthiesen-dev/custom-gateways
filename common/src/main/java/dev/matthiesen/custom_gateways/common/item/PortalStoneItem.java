package dev.matthiesen.custom_gateways.common.item;

import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.List;

public final class PortalStoneItem extends AbstractTransporter {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
        .thenLoop("animation.portal_stone.idle");

    public PortalStoneItem() {
        super(BlockRegistry.PORTAL_STONE.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.custom_gateways.portal_stone.destination_only"));
    }

    @Override
    public RawAnimation getIdleAnimation() {
        return IDLE_ANIM;
    }
}

