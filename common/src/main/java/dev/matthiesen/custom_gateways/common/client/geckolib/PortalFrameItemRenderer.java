package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.GeoItemRenderer;
import dev.matthiesen.custom_gateways.common.item.PortalFrameItem;
import dev.matthiesen.custom_gateways.common.util.GeoType;

public final class PortalFrameItemRenderer extends GeoItemRenderer<PortalFrameItem> {
    public PortalFrameItemRenderer() {
        super("portal_frame", GeoType.BLOCK, true, true);
    }
}
