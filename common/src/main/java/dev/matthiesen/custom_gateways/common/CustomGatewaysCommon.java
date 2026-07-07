package dev.matthiesen.custom_gateways.common;

import dev.matthiesen.common.matthiesen_lib_api.MatthiesenLibApi;
import dev.matthiesen.common.matthiesen_lib_api.abstracts.AbstractCommonMod;
import dev.matthiesen.libs.faststats.Token;
import org.jetbrains.annotations.Nullable;

public class CustomGatewaysCommon extends AbstractCommonMod {
    public static final String MOD_ID = "custom_gateways";
    public static final String MOD_NAME = "Custom Gateways";
    public static @Token final String METRICS_TOKEN = "f74eb1c8c94245013751f76d084cb249";

    public static final CustomGatewaysCommon INSTANCE = new CustomGatewaysCommon();

    public CustomGatewaysCommon() {
        super(MOD_ID, MOD_NAME);
    }

    @Override
    public @Nullable @Token String getMetricsToken() {
        return METRICS_TOKEN;
    }

    @Override
    public Runnable reload() {
        return () -> {
            // TODO
            createInfoLog("Reloaded");
        };
    }

    public void initialize() {
        super.initialize();

       if (MatthiesenLibApi.isModLoaded("cobblemon")) {
            createInfoLog("Cobblemon is loaded, Hello there Cobblemon!");
       }

        createInfoLog("Initialized");
    }
}
