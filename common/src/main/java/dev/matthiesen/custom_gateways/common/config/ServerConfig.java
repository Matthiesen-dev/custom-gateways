package dev.matthiesen.custom_gateways.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerConfig {

    // Teleport Validation
    public ModConfigSpec.LongValue teleportValidation_cooldownMS;
    public ModConfigSpec.IntValue teleportValidation_safeSearchRadius;
    public ModConfigSpec.BooleanValue teleportValidation_allowNonPlayerTeleport;

    // RemoteDialer Item Config
    public ModConfigSpec.IntValue remoteDialer_maxPortalEntries;

    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Custom Gateways Server Configuration").push("server");

        builder.comment("Teleport Validation Configuration").push("teleportValidation");
        teleportValidation_cooldownMS = builder.comment("The cooldown in milliseconds between teleportation attempts")
                .defineInRange("cooldownMS", 5000L, 0L, Long.MAX_VALUE);
        teleportValidation_safeSearchRadius = builder.comment("The radius in blocks to search for a safe teleportation location")
                .defineInRange("safeSearchRadius", 5, 0, Integer.MAX_VALUE);
        teleportValidation_allowNonPlayerTeleport = builder.comment("Whether to allow non-player entities to teleport using the custom gateways")
                .define("allowNonPlayerTeleport", false);
        builder.pop(); // Closes "teleportValidation"

        builder.comment("Remote Dialer Configuration").push("remoteDialer");
        remoteDialer_maxPortalEntries = builder.comment("The maximum number of portal entries that can be stored in a Remote Dialer item")
                .defineInRange("maxPortalEntries", 32, 1, Integer.MAX_VALUE);
        builder.pop(); // Closes "remoteDialer"

        builder.pop(); // Closes "server"
    }
}
