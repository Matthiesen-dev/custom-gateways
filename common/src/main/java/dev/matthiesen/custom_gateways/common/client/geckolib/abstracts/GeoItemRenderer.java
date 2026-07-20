package dev.matthiesen.custom_gateways.common.client.geckolib.abstracts;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class GeoItemRenderer<T extends Item & GeoAnimatable> {
    private final Renderer<T> renderer;

    public GeoItemRenderer(String name) {
        Model<T> model = new Model<>(name);
        this.renderer = new Renderer<>(model);
    }

    public Renderer<T> getRenderer() {
        return renderer;
    }

    public static class Model<T extends Item & GeoAnimatable> extends GeoModel<T> {
        private final String name;

        public Model(String name) {
            this.name = name;
        }

        @Override
        public ResourceLocation getModelResource(T animatable) {
            return CustomGatewaysCommon.modResource("geo/block/" + name + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(T animatable) {
            return CustomGatewaysCommon.modResource("textures/block/" + name + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(T animatable) {
            return CustomGatewaysCommon.modResource("animations/block/" + name + ".animation.json");
        }
    }

    public static class Renderer<T extends Item & GeoAnimatable> extends software.bernie.geckolib.renderer.GeoItemRenderer<T> {
        public Renderer(GeoModel<T> model) {
            super(model);
        }
    }
}
