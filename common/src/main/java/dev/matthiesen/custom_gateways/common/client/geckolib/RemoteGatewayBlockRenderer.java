package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.block.entity.RemoteGatewayBlockEntity;
import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.GeoBlockRenderer;

public final class RemoteGatewayBlockRenderer extends GeoBlockRenderer<RemoteGatewayBlockEntity> {
    public RemoteGatewayBlockRenderer() {
        super("remote_gateway", true, true);
    }

    public static final RemoteGatewayBlockRenderer INSTANCE = new RemoteGatewayBlockRenderer();
}

