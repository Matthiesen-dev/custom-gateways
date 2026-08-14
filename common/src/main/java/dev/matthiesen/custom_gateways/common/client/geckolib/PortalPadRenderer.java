package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.block.entity.PortalPadEntity;
import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.AbstractGeoBlockRenderer;
import dev.matthiesen.custom_gateways.common.item.PortalPadItem;

public final class PortalPadRenderer extends AbstractGeoBlockRenderer<PortalPadEntity, PortalPadItem> {
    public PortalPadRenderer() {
        super("portal_pad");
    }

    public static final PortalPadRenderer INSTANCE = new PortalPadRenderer();
}
