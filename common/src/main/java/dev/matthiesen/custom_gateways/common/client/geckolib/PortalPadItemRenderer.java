package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.GeoItemRenderer;
import dev.matthiesen.custom_gateways.common.item.PortalPadItem;
import dev.matthiesen.custom_gateways.common.util.GeoType;

public final class PortalPadItemRenderer extends GeoItemRenderer<PortalPadItem> {
    public PortalPadItemRenderer() {
        super("portal_pad", GeoType.BLOCK);
    }
}

