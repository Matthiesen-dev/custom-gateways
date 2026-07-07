package dev.matthiesen.custom_gateways.common.client.renderer.item;

import dev.matthiesen.custom_gateways.common.client.model.item.PortalFrameItemModel;
import dev.matthiesen.custom_gateways.common.item.PortalFrameItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class PortalFrameItemRenderer extends GeoItemRenderer<PortalFrameItem> {
    public PortalFrameItemRenderer() {
        super(new PortalFrameItemModel());
    }
}
