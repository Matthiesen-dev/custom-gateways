package dev.matthiesen.custom_gateways.common.util;

import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player teleport-warmup state for a single portal entity type.
 *
 * <p>Usage: each portal entity class that needs warmup teleportation should hold
 * one {@code static final PortalWarmupTracker WARMUP_TRACKER = new PortalWarmupTracker();}
 * and call {@link #cleanupStale}, {@link #processPlayer}, and {@link #remove} from its
 * {@code tick} method.
 */
public final class PortalWarmupTracker {
    public static final long TELEPORT_WARMUP_TICKS = 60L; // 3 s at 20 TPS
    private static final long WARMUP_STALE_TICKS = 5L;

    private final Map<UUID, PlayerWarmup> playerWarmups = new HashMap<>();

    /** Removes entries for players who have not been seen for {@value #WARMUP_STALE_TICKS} ticks. */
    public void cleanupStale(long gameTime) {
        playerWarmups.entrySet().removeIf(entry ->
            gameTime - entry.getValue().lastSeenTick > WARMUP_STALE_TICKS);
    }

    /**
     * Updates the warmup state for {@code player} at the given {@code portal}.
     *
     * @return {@code true} if the warmup is complete and the player should be teleported;
     *         {@code false} if the player is still counting down (a message is sent to the player).
     */
    public boolean processPlayer(ServerPlayer player, PortalRegistry.PortalLocation portal, long gameTime) {
        PlayerWarmup warmup = playerWarmups.get(player.getUUID());
        if (warmup == null || !warmup.portalLocation.equals(portal)) {
            playerWarmups.put(player.getUUID(), new PlayerWarmup(portal, gameTime, gameTime));
            warmup = playerWarmups.get(player.getUUID());
        } else {
            warmup.lastSeenTick = gameTime;
        }

        long remainingTicks = TELEPORT_WARMUP_TICKS - (gameTime - warmup.startedTick);
        if (remainingTicks > 0L) {
            player.sendSystemMessage(
                Component.literal(String.format("Teleporting in %.1fs", remainingTicks / 20.0D)), true);
            return false;
        }

        playerWarmups.remove(player.getUUID());
        return true;
    }

    /** Discards any warmup state for the given player UUID (e.g. when a cooldown is active). */
    public void remove(UUID uuid) {
        playerWarmups.remove(uuid);
    }

    private static final class PlayerWarmup {
        private final PortalRegistry.PortalLocation portalLocation;
        private final long startedTick;
        private long lastSeenTick;

        private PlayerWarmup(PortalRegistry.PortalLocation portalLocation, long startedTick, long lastSeenTick) {
            this.portalLocation = portalLocation;
            this.startedTick = startedTick;
            this.lastSeenTick = lastSeenTick;
        }
    }
}

