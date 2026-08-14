package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.GeoItemRenderer;
import dev.matthiesen.custom_gateways.common.item.AncientPortalItem;
import dev.matthiesen.custom_gateways.common.util.GeoType;

public final class AncientPortalItemRenderer extends GeoItemRenderer<AncientPortalItem> {
    public AncientPortalItemRenderer() {
        super("ancient_portal", GeoType.BLOCK, true, true);
    }
}

