package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.block.entity.PortalPadEntity;
import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.GeoBlockRenderer;

public final class PortalPadBlockRenderer extends GeoBlockRenderer<PortalPadEntity> {
    public PortalPadBlockRenderer() {
        super("portal_pad");
    }
}

