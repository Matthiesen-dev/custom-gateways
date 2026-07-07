package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.common.matthiesen_lib.registry.AbstractItemRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;

public class ItemRegistry extends AbstractItemRegistry {
    private static final ItemRegistry INSTANCE = new ItemRegistry();

    protected ItemRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}
}
