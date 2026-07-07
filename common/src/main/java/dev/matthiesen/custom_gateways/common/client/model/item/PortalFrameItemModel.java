package dev.matthiesen.custom_gateways.common.client.model.item;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.item.PortalFrameItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class PortalFrameItemModel extends GeoModel<PortalFrameItem> {
    @Override
    public ResourceLocation getModelResource(PortalFrameItem animatable) {
        return CustomGatewaysCommon.modResource("geo/block/portal_frame.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PortalFrameItem animatable) {
        return CustomGatewaysCommon.modResource("textures/block/portal_frame.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PortalFrameItem animatable) {
        return CustomGatewaysCommon.modResource("animations/block/portal_frame.animation.json");
    }
}
