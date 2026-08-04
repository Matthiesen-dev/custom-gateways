package dev.matthiesen.custom_gateways.common.config;

import com.google.gson.annotations.SerializedName;

public final class ServerConfig {

    @SerializedName("teleportValidation")
    public TeleportValidation teleportValidation = new TeleportValidation();

    @SerializedName("remoteDialerItemConfig")
    public RemoteDialerItemConfig remoteDialerItemConfig = new RemoteDialerItemConfig();

    public static class TeleportValidation {

        @SerializedName("cooldownMS")
        public long cooldownMS = 5000; // 5 seconds default

        @SerializedName("safeSearchRadius")
        public int safeSearchRadius = 5;

        @SerializedName("allowNonPlayerTeleport")
        public boolean allowNonPlayerTeleport = false;
    }

    public static class RemoteDialerItemConfig {

        @SerializedName("maxPortalEntries")
        public int maxPortalEntries = 32;
    }
}
