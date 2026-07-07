package dev.matthiesen.custom_gateways.common.client.renderer.block;

import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.client.model.block.PortalFrameBlockModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class PortalFrameBlockRenderer extends GeoBlockRenderer<PortalFrameEntity> {
    public PortalFrameBlockRenderer() {
        super(new PortalFrameBlockModel());
    }
}
