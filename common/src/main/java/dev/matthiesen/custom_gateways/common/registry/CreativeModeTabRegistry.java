package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.common.matthiesen_lib.registry.AbstractCreativeModeTabRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;

public class CreativeModeTabRegistry extends AbstractCreativeModeTabRegistry {
    private static final CreativeModeTabRegistry INSTANCE = new CreativeModeTabRegistry();

    protected CreativeModeTabRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}
}
