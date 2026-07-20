package dev.matthiesen.custom_gateways.common;

import dev.matthiesen.common.matthiesen_lib_api.abstracts.AbstractCommonMod;
import dev.matthiesen.custom_gateways.common.registry.*;
import dev.matthiesen.libs.faststats.Token;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class CustomGatewaysCommon extends AbstractCommonMod {
    public static final String MOD_ID = "custom_gateways";
    public static final String MOD_NAME = "Custom Gateways";
    public static @Token final String METRICS_TOKEN = "f74eb1c8c94245013751f76d084cb249";

    public static ResourceLocation modResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final CustomGatewaysCommon INSTANCE = new CustomGatewaysCommon();

    public CustomGatewaysCommon() {
        super(MOD_ID, MOD_NAME);
    }

    public void initialize() {
        super.initialize();

        BlockRegistry.init();
        BlockEntityRegistry.init();
        ItemRegistry.init();
        CreativeModeTabRegistry.init();
        SoundRegistry.init();

        createInfoLog("Initialized Common");
    }

    @Override
    public Runnable reload() {
        return () -> {
            // TODO
            createInfoLog("Reloaded");
        };
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }
}
