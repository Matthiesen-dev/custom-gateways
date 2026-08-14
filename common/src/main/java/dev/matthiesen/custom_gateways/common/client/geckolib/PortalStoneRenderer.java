package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.block.entity.PortalStoneEntity;
import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.AbstractGeoBlockRenderer;
import dev.matthiesen.custom_gateways.common.item.PortalStoneItem;

public final class PortalStoneRenderer extends AbstractGeoBlockRenderer<PortalStoneEntity, PortalStoneItem> {
    public PortalStoneRenderer() {
        super("portal_stone", false, true);
    }
}
