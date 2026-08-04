package dev.matthiesen.custom_gateways.common.util;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player teleportation cooldowns to prevent spam teleportation
 */
public final class PlayerCooldownTracker {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    private static long getCooldownDurationMs() {
        return CustomGatewaysCommon.INSTANCE.getServerConfig().teleportValidation.cooldownMS;
    }

    /**
     * Checks if a player is on cooldown
     */
    public static boolean isOnCooldown(Player player) {
        UUID uuid = player.getUUID();
        Long cooldownEnd = cooldowns.get(uuid);

        if (cooldownEnd == null) {
            return false;
        }

        if (System.currentTimeMillis() >= cooldownEnd) {
            cooldowns.remove(uuid);
            return false;
        }

        return true;
    }

    /**
     * Sets a player on cooldown
     */
    public static void setCooldown(Player player) {
        UUID uuid = player.getUUID();
        cooldowns.put(uuid, System.currentTimeMillis() + getCooldownDurationMs());
    }
}

