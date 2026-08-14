package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.block.entity.AncientPortalEntity;
import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.AbstractGeoBlockRenderer;
import dev.matthiesen.custom_gateways.common.item.AncientPortalItem;

public final class AncientPortalRenderer extends AbstractGeoBlockRenderer<AncientPortalEntity, AncientPortalItem> {
    public AncientPortalRenderer() {
        super("ancient_portal", true, true);
    }

    public static final AncientPortalRenderer INSTANCE = new AncientPortalRenderer();
}
