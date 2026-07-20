package dev.matthiesen.custom_gateways.common.item;

import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.mutable.MutableObject;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.List;
import java.util.function.Consumer;

public final class PortalPadItem extends BlockItem implements GeoItem {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
        .thenLoop("animation.portal_pad.idle");
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public final MutableObject<GeoRenderProvider> renderProviderHolder = new MutableObject<>();

    public PortalPadItem() {
        super(BlockRegistry.PORTAL_PAD.get(), new Properties());
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.renderProviderHolder.getValue());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state -> state.setAndContinue(IDLE_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.custom_gateways.portal_pad.destination_only"));
    }
}

