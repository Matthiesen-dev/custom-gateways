package dev.matthiesen.custom_gateways.neoforge;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.neoforged.fml.common.Mod;

@Mod(CustomGatewaysCommon.MOD_ID)
public class CustomGatewaysNeoForge {
    public CustomGatewaysNeoForge() {
        var instance = CustomGatewaysCommon.INSTANCE;
        instance.createInfoLog("Loading for NeoForge Mod Loader");
        instance.initialize();
    }
}
