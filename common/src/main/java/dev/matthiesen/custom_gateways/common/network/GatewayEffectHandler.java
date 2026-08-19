package dev.matthiesen.custom_gateways.common.network;

import dev.matthiesen.matthiesen_core.common.api.client.particle.ParticleSpawner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class GatewayEffectHandler {
    private static final int STALE_EFFECT_TICKS = 30;

    private static final Map<GatewayEffectKey, ActiveGatewayEffect> ACTIVE_EFFECTS = new HashMap<>();

    private GatewayEffectHandler() {
    }

    public static void handle(GatewayEffectPayload payload) {
        GatewayEffectKey key = new GatewayEffectKey(payload.effectType(), payload.sourceEntityId());
        if (payload.phase() == GatewayEffectPayload.PHASE_STOP) {
            ACTIVE_EFFECTS.remove(key);
            return;
        }

        ACTIVE_EFFECTS.compute(key, (ignored, existing) -> {
            if (existing == null) {
                return new ActiveGatewayEffect(
                    payload.effectType(),
                    payload.center(),
                    payload.serverGameTime(),
                    payload.progress(),
                    payload.durationTicks()
                );
            }

            existing.center = payload.center();
            existing.serverGameTimeAtSync = payload.serverGameTime();
            existing.progressAtSync = payload.progress();
            existing.durationTicks = payload.durationTicks();
            existing.clientLevelGameTimeAtSync = Long.MIN_VALUE;
            return existing;
        });
    }

    public static void tick(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null) {
            ACTIVE_EFFECTS.clear();
            return;
        }

        long clientGameTime = level.getGameTime();
        Iterator<Map.Entry<GatewayEffectKey, ActiveGatewayEffect>> iterator = ACTIVE_EFFECTS.entrySet().iterator();
        while (iterator.hasNext()) {
            ActiveGatewayEffect effect = iterator.next().getValue();

            if (effect.clientLevelGameTimeAtSync == Long.MIN_VALUE) {
                effect.clientLevelGameTimeAtSync = clientGameTime;
            }

            long localTicksSinceSync = clientGameTime - effect.clientLevelGameTimeAtSync;
            if (localTicksSinceSync > STALE_EFFECT_TICKS) {
                iterator.remove();
                continue;
            }

            renderEffect(level, effect, localTicksSinceSync);
        }
    }

    public static void clear() {
        ACTIVE_EFFECTS.clear();
    }

    private static void renderEffect(ClientLevel level, ActiveGatewayEffect effect, long localTicksSinceSync) {
        if (effect.effectType == GatewayEffectPayload.EFFECT_NETHER_GATE_WARMUP) {
            float progress = effect.durationTicks <= 0
                ? effect.progressAtSync
                : Math.min(1.0F, effect.progressAtSync + (float) localTicksSinceSync / (float) effect.durationTicks);
            long renderGameTime = effect.serverGameTimeAtSync + localTicksSinceSync;
            netherGateWarmupParticles(level, effect.center, renderGameTime, progress);
        }
    }

    private static void netherGateWarmupParticles(ClientLevel level, Vec3 center, long gameTime, float progress) {
        double baseAngle = (gameTime * 0.25D) % (2.0D * Math.PI);

        double radius = 0.7D - progress * 0.3D;
        int rings = 2;
        int particlesPerRing = 5;
        double ringSpacing = 0.5D;

        for (int ring = 0; ring < rings; ring++) {
            double heightOffset = ((gameTime * 0.08D + ring * ringSpacing) % 2.2D);

            for (int i = 0; i < particlesPerRing; i++) {
                double angle = baseAngle + (2.0D * Math.PI * i / particlesPerRing) + ring * 0.63D;
                Vec3 particlePosition = center.add(Math.cos(angle) * radius, heightOffset, Math.sin(angle) * radius);

                ParticleSpawner spawner = buildParticleSpawner(level, (i + ring) % 2 == 0);
                spawner.spawnParticle(particlePosition, new Vec3(0.0D, 0.1D, 0.0D));
            }
        }
    }

    private static ParticleSpawner buildParticleSpawner(ClientLevel level, boolean useSoulFire) {
        return ParticleSpawner.of(level, useSoulFire ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME);
    }

    private record GatewayEffectKey(int effectType, int sourceEntityId) {
    }

    private static final class ActiveGatewayEffect {
        private final int effectType;
        private Vec3 center;
        private long serverGameTimeAtSync;
        private long clientLevelGameTimeAtSync;
        private float progressAtSync;
        private int durationTicks;

        private ActiveGatewayEffect(int effectType, Vec3 center, long serverGameTimeAtSync, float progressAtSync, int durationTicks) {
            this.effectType = effectType;
            this.center = center;
            this.serverGameTimeAtSync = serverGameTimeAtSync;
            this.clientLevelGameTimeAtSync = Long.MIN_VALUE;
            this.progressAtSync = progressAtSync;
            this.durationTicks = durationTicks;
        }
    }
}


