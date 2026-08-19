package dev.matthiesen.custom_gateways.common.util;

import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import dev.matthiesen.custom_gateways.common.network.GatewayEffectPayload;
import dev.matthiesen.custom_gateways.common.registry.NetworkRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Reusable server-side lifecycle tracker for gateway warmup visual effects.
 *
 * <p>Each gateway type can keep one static instance configured for its effect type,
 * warmup duration, sync interval, and observer radius. The tracker handles emitting
 * start/sync/stop packets and cleaning up stale active effect state across multiple
 * portal instances of the same gateway type.</p>
 */
public final class GatewayWarmupEffectTracker {
    private final int effectType;
    private final int durationTicks;
    private final int syncIntervalTicks;
    private final double syncDistanceSqr;
    private final double broadcastRadius;
    private final Map<UUID, ActiveWarmupEffect> activeEffects = new HashMap<>();

    public GatewayWarmupEffectTracker(int effectType, int durationTicks, int syncIntervalTicks, double syncDistanceSqr, double broadcastRadius) {
        this.effectType = effectType;
        this.durationTicks = durationTicks;
        this.syncIntervalTicks = syncIntervalTicks;
        this.syncDistanceSqr = syncDistanceSqr;
        this.broadcastRadius = broadcastRadius;
    }

    public void sync(ServerLevel level, PortalRegistry.PortalLocation portalLocation, ServerPlayer player, Vec3 center, long gameTime, float progress) {
        ActiveWarmupEffect state = activeEffects.get(player.getUUID());
        if (state != null && !state.portalLocation.equals(portalLocation)) {
            send(level, GatewayEffectPayload.PHASE_STOP, state.sourceEntityId, state.lastCenter, gameTime, progress);
            activeEffects.remove(player.getUUID());
            state = null;
        }

        if (state == null) {
            activeEffects.put(player.getUUID(), new ActiveWarmupEffect(portalLocation, player.getId(), center, gameTime));
            send(level, GatewayEffectPayload.PHASE_START, player.getId(), center, gameTime, progress);
            return;
        }

        boolean shouldSync = gameTime - state.lastSyncTick >= syncIntervalTicks
            || state.lastCenter.distanceToSqr(center) >= syncDistanceSqr;

        state.lastCenter = center;
        if (!shouldSync) {
            return;
        }

        state.lastSyncTick = gameTime;
        send(level, GatewayEffectPayload.PHASE_SYNC, player.getId(), center, gameTime, progress);
    }

    public void stop(ServerLevel level, PortalRegistry.PortalLocation portalLocation, UUID playerUuid, @Nullable Vec3 fallbackCenter, int fallbackSourceEntityId) {
        ActiveWarmupEffect state = activeEffects.get(playerUuid);
        if (state == null || !state.portalLocation.equals(portalLocation)) {
            return;
        }

        activeEffects.remove(playerUuid);
        send(
            level,
            GatewayEffectPayload.PHASE_STOP,
            state.sourceEntityId > 0 ? state.sourceEntityId : fallbackSourceEntityId,
            fallbackCenter != null ? fallbackCenter : state.lastCenter,
            level.getGameTime(),
            1.0F
        );
    }

    public void stopMissing(ServerLevel level, PortalRegistry.PortalLocation portalLocation, Set<UUID> playersStillWarmingUp) {
        Iterator<Map.Entry<UUID, ActiveWarmupEffect>> iterator = activeEffects.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveWarmupEffect> entry = iterator.next();
            ActiveWarmupEffect state = entry.getValue();
            if (!state.portalLocation.equals(portalLocation) || playersStillWarmingUp.contains(entry.getKey())) {
                continue;
            }

            iterator.remove();
            send(level, GatewayEffectPayload.PHASE_STOP, state.sourceEntityId, state.lastCenter, level.getGameTime(), 1.0F);
        }
    }

    public void stopPortal(ServerLevel level, PortalRegistry.PortalLocation portalLocation) {
        Iterator<Map.Entry<UUID, ActiveWarmupEffect>> iterator = activeEffects.entrySet().iterator();
        while (iterator.hasNext()) {
            ActiveWarmupEffect state = iterator.next().getValue();
            if (!state.portalLocation.equals(portalLocation)) {
                continue;
            }

            iterator.remove();
            send(level, GatewayEffectPayload.PHASE_STOP, state.sourceEntityId, state.lastCenter, level.getGameTime(), 1.0F);
        }
    }

    private void send(ServerLevel level, int phase, int sourceEntityId, Vec3 center, long gameTime, float progress) {
        NetworkRegistry.sendToNearbyPlayers(
            level,
            center,
            broadcastRadius,
            new GatewayEffectPayload(effectType, phase, sourceEntityId, center, gameTime, progress, durationTicks)
        );
    }

    private static final class ActiveWarmupEffect {
        private final PortalRegistry.PortalLocation portalLocation;
        private final int sourceEntityId;
        private Vec3 lastCenter;
        private long lastSyncTick;

        private ActiveWarmupEffect(PortalRegistry.PortalLocation portalLocation, int sourceEntityId, Vec3 lastCenter, long lastSyncTick) {
            this.portalLocation = portalLocation;
            this.sourceEntityId = sourceEntityId;
            this.lastCenter = lastCenter;
            this.lastSyncTick = lastSyncTick;
        }
    }
}

