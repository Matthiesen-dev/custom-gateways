package dev.matthiesen.custom_gateways.neoforge;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.neoforged.fml.common.Mod;

@Mod(CustomGatewaysCommon.MOD_ID)
public class CustomGatewaysNeoForge {

    public CustomGatewaysNeoForge() {
        var INSTANCE = CustomGatewaysCommon.INSTANCE;
        INSTANCE.createInfoLog("Loading for NeoForge Mod Loader");
        INSTANCE.initialize();
    }
}
