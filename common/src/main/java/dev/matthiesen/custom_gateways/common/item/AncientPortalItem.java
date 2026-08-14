package dev.matthiesen.custom_gateways.common.item;

import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import net.minecraft.world.item.BlockItem;
import org.apache.commons.lang3.mutable.MutableObject;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.function.Consumer;

public final class AncientPortalItem extends BlockItem implements GeoItem, DimensionalGateItem {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
        .thenLoop("animation.ancient_portal.linked");
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public final MutableObject<GeoRenderProvider> renderProviderHolder = new MutableObject<>();

    public AncientPortalItem() {
        super(BlockRegistry.ANCIENT_PORTAL.get(), new Properties());
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
        return this.cache;
    }
}

