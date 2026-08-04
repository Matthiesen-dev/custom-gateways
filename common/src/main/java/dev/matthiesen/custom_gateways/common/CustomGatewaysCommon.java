package dev.matthiesen.custom_gateways.common;

import dev.matthiesen.custom_gateways.common.config.ServerConfig;
import dev.matthiesen.custom_gateways.common.registry.*;
import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import dev.matthiesen.matthiesen_core.common.api.events.PlatformEvents;
import dev.matthiesen.matthiesen_core.common.utility.config.ConfigManager;
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

    private static final ConfigManager<ServerConfig> SERVER_CONFIG_MANAGER =
            INSTANCE.createConfigManager(ServerConfig.class, "server");

    public CustomGatewaysCommon() {
        super(MOD_ID, MOD_NAME);
    }

    public void initialize() {
        super.initialize();
        SERVER_CONFIG_MANAGER.loadConfig();

        BlockRegistry.init();
        BlockEntityRegistry.init();
        ItemRegistry.init();
        CreativeModeTabRegistry.init();
        SoundRegistry.init();
        CriterionRegistry.init();
        MenuRegistry.init();
        NetworkRegistry.init(this);

        PlatformEvents.SERVER_RELOAD.subscribe(event -> {
            SERVER_CONFIG_MANAGER.loadConfig();
            createInfoLog("Reloaded configs");
        });

        createInfoLog("Initialized Common");
    }

    public ServerConfig getServerConfig() {
        return SERVER_CONFIG_MANAGER.getConfig();
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }
}
