package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.GeoItemRenderer;
import dev.matthiesen.custom_gateways.common.item.PortalStoneItem;
import dev.matthiesen.custom_gateways.common.util.GeoType;

public final class PortalStoneItemRenderer extends GeoItemRenderer<PortalStoneItem> {
    public PortalStoneItemRenderer() {
        super("portal_stone", GeoType.BLOCK, false, true);
    }
}

