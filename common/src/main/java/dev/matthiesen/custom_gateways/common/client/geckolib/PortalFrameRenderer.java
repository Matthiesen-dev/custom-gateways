package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.AbstractGeoBlockRenderer;
import dev.matthiesen.custom_gateways.common.item.PortalFrameItem;

public final class PortalFrameRenderer extends AbstractGeoBlockRenderer<PortalFrameEntity, PortalFrameItem> {
    public PortalFrameRenderer() {
        super("portal_frame", true, true);
    }

    public static final PortalFrameRenderer INSTANCE = new PortalFrameRenderer();
}
