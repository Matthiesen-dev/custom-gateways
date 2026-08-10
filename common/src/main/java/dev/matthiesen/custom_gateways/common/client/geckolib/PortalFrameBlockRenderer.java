package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.GeoBlockRenderer;

public final class PortalFrameBlockRenderer extends GeoBlockRenderer<PortalFrameEntity> {
    public PortalFrameBlockRenderer() {
        super("portal_frame", true, true);
    }
}
