package dev.matthiesen.custom_gateways.common.client.geckolib;

import dev.matthiesen.custom_gateways.common.block.entity.NetherGateEntity;
import dev.matthiesen.custom_gateways.common.client.geckolib.abstracts.AbstractGeoBlockRenderer;
import dev.matthiesen.custom_gateways.common.item.NetherGateItem;

public final class NetherGateRenderer extends AbstractGeoBlockRenderer<NetherGateEntity, NetherGateItem> {
    public NetherGateRenderer() {
        super("nether_gate", true, true);
    }

    public static final NetherGateRenderer INSTANCE = new NetherGateRenderer();
}

