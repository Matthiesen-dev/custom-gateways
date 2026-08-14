package dev.matthiesen.custom_gateways.common.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.mutable.MutableObject;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.function.Consumer;

public abstract class AbstractTransporter extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    public final MutableObject<GeoRenderProvider> renderProviderHolder = new MutableObject<>();

    public abstract RawAnimation getIdleAnimation();

    public AbstractTransporter(Block block) {
        super(block, new Properties());
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.renderProviderHolder.getValue());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state ->
                state.setAndContinue(getIdleAnimation())
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
