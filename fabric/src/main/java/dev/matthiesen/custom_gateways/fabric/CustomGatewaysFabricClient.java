package dev.matthiesen.custom_gateways.fabric;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommonClient;
import net.fabricmc.api.ClientModInitializer;

public class CustomGatewaysFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        var INSTANCE = CustomGatewaysCommonClient.INSTANCE;
        INSTANCE.createInfoLog("Loading for Fabric Mod Loader (Client)");
        INSTANCE.initialize();
        INSTANCE.registerRenderers();
    }
}
