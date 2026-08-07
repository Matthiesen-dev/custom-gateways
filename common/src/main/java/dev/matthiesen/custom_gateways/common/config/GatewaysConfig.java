package dev.matthiesen.custom_gateways.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class GatewaysConfig {
    public static final ServerConfig SERVER_CONFIG;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER_CONFIG = specPair.getLeft();
    }
}
