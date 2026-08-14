package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.block.entity.PortalStoneEntity;
import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.GeoBlockRenderer;

public final class PortalStoneBlockRenderer extends GeoBlockRenderer<PortalStoneEntity> {
    public PortalStoneBlockRenderer() {
        super("portal_stone", false, true);
    }
}

