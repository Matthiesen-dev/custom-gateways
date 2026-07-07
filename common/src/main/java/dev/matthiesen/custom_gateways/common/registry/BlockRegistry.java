package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.common.matthiesen_lib.registry.AbstractBlockRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;

public class BlockRegistry extends AbstractBlockRegistry {
    private static final BlockRegistry INSTANCE = new BlockRegistry();

    protected BlockRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}
}
