package dev.matthiesen.custom_gateways.common;

import dev.matthiesen.custom_gateways.common.config.GatewaysConfig;
import dev.matthiesen.custom_gateways.common.registry.*;
import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import dev.matthiesen.matthiesen_core.common.api.platform.loader.ModConfigType;
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

        registerModConfig(MOD_ID, ModConfigType.SERVER, GatewaysConfig.SERVER_SPEC, "custom_gateways/server.toml");

        BlockRegistry.init();
        BlockEntityRegistry.init();
        ItemRegistry.init();
        CreativeModeTabRegistry.init();
        SoundRegistry.init();
        CriterionRegistry.init();
        MenuRegistry.init();
        NetworkRegistry.init(this);

        createInfoLog("Initialized Common");
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }
}
