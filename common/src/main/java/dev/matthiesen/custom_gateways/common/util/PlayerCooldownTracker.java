package dev.matthiesen.custom_gateways.common.util;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player teleportation cooldowns to prevent spam teleportation
 */
public final class PlayerCooldownTracker {
    private static final long COOLDOWN_DURATION_MS = 5000; // 5 seconds default
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

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
     * Gets remaining cooldown time in seconds
     */
    public static double getRemainingCooldownSeconds(Player player) {
        UUID uuid = player.getUUID();
        Long cooldownEnd = cooldowns.get(uuid);

        if (cooldownEnd == null) {
            return 0;
        }

        long remainingMs = cooldownEnd - System.currentTimeMillis();
        return Math.max(0, remainingMs / 1000.0);
    }

    /**
     * Sets a player on cooldown
     */
    public static void setCooldown(Player player) {
        UUID uuid = player.getUUID();
        cooldowns.put(uuid, System.currentTimeMillis() + COOLDOWN_DURATION_MS);
    }

    /**
     * Sets a player on cooldown with custom duration
     */
    public static void setCooldown(Player player, long durationMs) {
        UUID uuid = player.getUUID();
        cooldowns.put(uuid, System.currentTimeMillis() + durationMs);
    }

    /**
     * Clears a player's cooldown
     */
    public static void clearCooldown(Player player) {
        cooldowns.remove(player.getUUID());
    }

    /**
     * Clears all cooldowns (for server shutdown, etc.)
     */
    public static void clearAllCooldowns() {
        cooldowns.clear();
    }
}

