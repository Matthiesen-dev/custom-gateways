package dev.matthiesen.custom_gateways.common;

public class CustomGatewaysCommonClient {
    public static void initialize() {
        CustomGatewaysCommon.INSTANCE.createInfoLog("Loading client-side for " + CustomGatewaysCommon.MOD_NAME);
    }
}
