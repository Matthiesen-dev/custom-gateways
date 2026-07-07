package dev.matthiesen.custom_gateways.fabric;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.fabricmc.api.ModInitializer;

public class CustomGatewaysFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        var INSTANCE = CustomGatewaysCommon.INSTANCE;
        INSTANCE.createInfoLog("Loading for Fabric Mod Loader");
        INSTANCE.initialize();
    }
}
