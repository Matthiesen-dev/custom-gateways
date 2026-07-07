package dev.matthiesen.custom_gateways.common.client.model.block;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class PortalFrameBlockModel extends GeoModel<PortalFrameEntity> {

    @Override
    public ResourceLocation getModelResource(PortalFrameEntity animatable) {
        return CustomGatewaysCommon.modResource("geo/block/portal_frame.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PortalFrameEntity animatable) {
        return CustomGatewaysCommon.modResource("textures/block/portal_frame.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PortalFrameEntity animatable) {
        return CustomGatewaysCommon.modResource("animations/block/portal_frame.animation.json");
    }
}
