package dev.matthiesen.custom_gateways.common.client.geckolib.abstracts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class GeoBlockRenderer<T extends BlockEntity & GeoAnimatable> {
    private final Renderer<T> renderer;

    public GeoBlockRenderer(String name, boolean isTransparent, boolean isEmissive) {
        Model<T> model = new Model<>(name);
        this.renderer = new Renderer<>(model, isTransparent, isEmissive);
    }

    public GeoBlockRenderer(String name, boolean isTransparent) {
        Model<T> model = new Model<>(name);
        this.renderer = new Renderer<>(model, isTransparent, false);
    }

    public GeoBlockRenderer(String name) {
        Model<T> model = new Model<>(name);
        this.renderer = new Renderer<>(model, false, false);
    }

    public Renderer<T> getRenderer() {
        return renderer;
    }

    public static class Model<T extends BlockEntity & GeoAnimatable> extends GeoModel<T> {
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

    public static class Renderer<T extends BlockEntity & GeoAnimatable> extends software.bernie.geckolib.renderer.GeoBlockRenderer<T> {
        private final boolean isTransparent;

        public Renderer(GeoModel<T> model, boolean isTransparent, boolean isEmissive) {
            super(model);
            this.isTransparent = isTransparent;
            if (isEmissive) {
                addRenderLayer(new AutoGlowingGeoLayer<>(this));
            }
        }

        @Override
        public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
            if (animatable instanceof PortalFrameEntity portalFrameEntity) {
                if (portalFrameEntity.isSlave()) return;
            }
            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        }

        @Override
        public @Nullable RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
            if (isTransparent) {
                return RenderType.entityTranslucent(getTextureLocation(animatable));
            }
            return super.getRenderType(animatable, texture, bufferSource, partialTick);
        }
    }
}
