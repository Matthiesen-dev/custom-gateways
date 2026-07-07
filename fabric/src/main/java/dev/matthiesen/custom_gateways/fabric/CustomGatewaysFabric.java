package dev.matthiesen.custom_gateways.fabric;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.fabricmc.api.ModInitializer;

public class CustomGatewaysFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        var instance = CustomGatewaysCommon.INSTANCE;
        instance.createInfoLog("Loading for Fabric Mod Loader");
        instance.initialize();
    }
}
