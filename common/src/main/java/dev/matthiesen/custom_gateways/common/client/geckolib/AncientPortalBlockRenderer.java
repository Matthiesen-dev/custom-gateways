package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.block.entity.AncientPortalEntity;
import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.GeoBlockRenderer;

public final class AncientPortalBlockRenderer extends GeoBlockRenderer<AncientPortalEntity> {
    public AncientPortalBlockRenderer() {
        super("ancient_portal", true, true);
    }
}

