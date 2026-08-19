package dev.matthiesen.custom_gateways.common.block.entity;

import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import dev.matthiesen.custom_gateways.common.network.GatewayEffectPayload;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.registry.NetworkRegistry;
import dev.matthiesen.custom_gateways.common.util.PlayerCooldownTracker;
import dev.matthiesen.custom_gateways.common.util.PortalTeleporter;
import dev.matthiesen.custom_gateways.common.util.PortalWarmupTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class NetherGateEntity extends BlockEntity implements GeoBlockEntity, DimensionalGate {
    private static final PortalWarmupTracker WARMUP_TRACKER = new PortalWarmupTracker();
    private static final Map<UUID, WarmupEffectState> ACTIVE_WARMUP_EFFECTS = new HashMap<>();
    private static final int WARMUP_EFFECT_SYNC_INTERVAL_TICKS = 10;
    private static final double WARMUP_EFFECT_SYNC_DISTANCE_SQR = 0.0625D;
    private static final double WARMUP_EFFECT_BROADCAST_RADIUS = 24.0D;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
        .thenLoop("animation.nether_gate.idle");
    private static final RawAnimation LINKED_ANIM = RawAnimation.begin()
        .thenLoop("animation.nether_gate.linked");
    private static final RawAnimation LINK_ANIM = RawAnimation.begin()
        .thenPlay("animation.nether_gate.link");

    private boolean isLinked = false;
    private String dimension = "minecraft:overworld";
    private int x = 0;
    private int y = 0;
    private int z = 0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public NetherGateEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.NETHER_GATE_BE.get(), pos, state);
    }

    // ------------------------------------------------------------------ state

    public boolean isLinked() {
        return isLinked;
    }

    public void setLinkedTarget(ResourceLocation dimension, BlockPos targetPos, boolean triggerLinkAnimation) {
        boolean changed = !this.isLinked
            || !this.dimension.equals(dimension.toString())
            || this.x != targetPos.getX()
            || this.y != targetPos.getY()
            || this.z != targetPos.getZ();

        this.isLinked = true;
        this.dimension = dimension.toString();
        this.x = targetPos.getX();
        this.y = targetPos.getY();
        this.z = targetPos.getZ();

        if (changed) {
            syncToClient();
        }

        if (triggerLinkAnimation) {
            this.triggerAnim("idle", "link");
        }
    }

    public void clearLinkedTarget() {
        if (!this.isLinked) {
            return;
        }
        this.isLinked = false;
        this.dimension = "minecraft:overworld";
        this.x = 0;
        this.y = 0;
        this.z = 0;
        syncToClient();
    }

    @Override
    public ResourceLocation getLinkedDimension() {
        return ResourceLocation.parse(dimension);
    }

    public BlockPos getLinkedPosition() {
        return new BlockPos(x, y, z);
    }

    // ------------------------------------------------------------------ sync

    private void syncToClient() {
        this.setChanged();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    // ------------------------------------------------------------------ NBT

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        CompoundTag tag = compoundTag.getCompound("portal_data");
        this.isLinked = tag.getBoolean("is_linked");
        this.dimension = tag.contains("dimension") ? tag.getString("dimension") : "minecraft:overworld";
        this.x = tag.getInt("x");
        this.y = tag.getInt("y");
        this.z = tag.getInt("z");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("is_linked", this.isLinked);
        tag.putString("dimension", this.dimension);
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        tag.putInt("z", this.z);
        compoundTag.put("portal_data", tag);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, provider);
        return tag;
    }

    // ---------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state ->
            state.setAndContinue(this.isLinked ? LINKED_ANIM : IDLE_ANIM))
            .triggerableAnim("link", LINK_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ------------------------------------------------------------------ tick

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (!(t instanceof NetherGateEntity entity)) return;
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        PortalRegistry.PortalLocation currentPortal =
            new PortalRegistry.PortalLocation(level.dimension().location(), blockPos);
        PortalRegistry registry = PortalRegistry.get(serverLevel);

        PortalRegistry.PortalLocation linkedPortal = registry.getLinkedPortal(currentPortal);
        if (linkedPortal == null) {
            stopWarmupEffectsForPortal(serverLevel, currentPortal);
            entity.clearLinkedTarget();
            return;
        }

        entity.setLinkedTarget(linkedPortal.dimension(), linkedPortal.getBlockPos(), false);

        AABB bounds = new AABB(
            blockPos.getX() + 0.05, blockPos.getY(), blockPos.getZ() + 0.05,
            blockPos.getX() + 0.95, blockPos.getY() + 0.5, blockPos.getZ() + 0.95
        );

        List<Entity> entities = level.getEntities(null, bounds);
        long gameTime = serverLevel.getGameTime();

        WARMUP_TRACKER.cleanupStale(gameTime);

        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, linkedPortal.dimension());
        ServerLevel targetLevel = serverLevel.getServer().getLevel(dimensionKey);
        if (targetLevel == null) {
            stopWarmupEffectsForPortal(serverLevel, currentPortal);
            return;
        }

        Set<UUID> playersStillWarmingUp = new HashSet<>();

        for (Entity entityInBounds : entities) {
            if (entityInBounds instanceof ServerPlayer player && PlayerCooldownTracker.isOnCooldown(player)) {
                WARMUP_TRACKER.remove(player.getUUID());
                stopWarmupEffect(serverLevel, currentPortal, player.getUUID(), player.position(), player.getId());
                continue;
            }

            if (entityInBounds instanceof ServerPlayer player) {
                if (!WARMUP_TRACKER.processPlayer(player, currentPortal, gameTime)) {
                    float progress = WARMUP_TRACKER.getWarmupProgress(player.getUUID(), gameTime);
                    syncWarmupEffect(serverLevel, currentPortal, player, player.position(), gameTime, progress);
                    playersStillWarmingUp.add(player.getUUID());
                    continue;
                }

                stopWarmupEffect(serverLevel, currentPortal, player.getUUID(), player.position(), player.getId());
            }

            PortalTeleporter.teleportEntity(entityInBounds, targetLevel, linkedPortal.getBlockPos());
        }

        stopMissingWarmupEffects(serverLevel, currentPortal, playersStillWarmingUp);
    }

    private static void syncWarmupEffect(ServerLevel level, PortalRegistry.PortalLocation portalLocation, ServerPlayer player, Vec3 center, long gameTime, float progress) {
        WarmupEffectState state = ACTIVE_WARMUP_EFFECTS.get(player.getUUID());
        if (state != null && !state.portalLocation.equals(portalLocation)) {
            sendWarmupEffect(level, GatewayEffectPayload.PHASE_STOP, state.sourceEntityId, state.lastCenter, gameTime, progress);
            ACTIVE_WARMUP_EFFECTS.remove(player.getUUID());
            state = null;
        }

        if (state == null) {
            ACTIVE_WARMUP_EFFECTS.put(player.getUUID(), new WarmupEffectState(portalLocation, player.getId(), center, gameTime));
            sendWarmupEffect(level, GatewayEffectPayload.PHASE_START, player.getId(), center, gameTime, progress);
            return;
        }

        boolean shouldSync = gameTime - state.lastSyncTick >= WARMUP_EFFECT_SYNC_INTERVAL_TICKS
            || state.lastCenter.distanceToSqr(center) >= WARMUP_EFFECT_SYNC_DISTANCE_SQR;

        state.lastCenter = center;
        if (!shouldSync) {
            return;
        }

        state.lastSyncTick = gameTime;
        sendWarmupEffect(level, GatewayEffectPayload.PHASE_SYNC, player.getId(), center, gameTime, progress);
    }

    private static void stopWarmupEffect(ServerLevel level, PortalRegistry.PortalLocation portalLocation, UUID playerUuid, Vec3 fallbackCenter, int fallbackSourceEntityId) {
        WarmupEffectState state = ACTIVE_WARMUP_EFFECTS.get(playerUuid);
        if (state == null || !state.portalLocation.equals(portalLocation)) {
            return;
        }

        ACTIVE_WARMUP_EFFECTS.remove(playerUuid);
        sendWarmupEffect(
            level,
            GatewayEffectPayload.PHASE_STOP,
            state.sourceEntityId > 0 ? state.sourceEntityId : fallbackSourceEntityId,
            fallbackCenter != null ? fallbackCenter : state.lastCenter,
            level.getGameTime(),
            1.0F
        );
    }

    private static void stopMissingWarmupEffects(ServerLevel level, PortalRegistry.PortalLocation portalLocation, Set<UUID> playersStillWarmingUp) {
        Iterator<Map.Entry<UUID, WarmupEffectState>> iterator = ACTIVE_WARMUP_EFFECTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, WarmupEffectState> entry = iterator.next();
            WarmupEffectState state = entry.getValue();
            if (!state.portalLocation.equals(portalLocation) || playersStillWarmingUp.contains(entry.getKey())) {
                continue;
            }

            iterator.remove();
            sendWarmupEffect(level, GatewayEffectPayload.PHASE_STOP, state.sourceEntityId, state.lastCenter, level.getGameTime(), 1.0F);
        }
    }

    private static void stopWarmupEffectsForPortal(ServerLevel level, PortalRegistry.PortalLocation portalLocation) {
        Iterator<Map.Entry<UUID, WarmupEffectState>> iterator = ACTIVE_WARMUP_EFFECTS.entrySet().iterator();
        while (iterator.hasNext()) {
            WarmupEffectState state = iterator.next().getValue();
            if (!state.portalLocation.equals(portalLocation)) {
                continue;
            }

            iterator.remove();
            sendWarmupEffect(level, GatewayEffectPayload.PHASE_STOP, state.sourceEntityId, state.lastCenter, level.getGameTime(), 1.0F);
        }
    }

    private static void sendWarmupEffect(ServerLevel level, int phase, int sourceEntityId, Vec3 center, long gameTime, float progress) {
        NetworkRegistry.sendToNearbyPlayers(
            level,
            center,
            WARMUP_EFFECT_BROADCAST_RADIUS,
            new GatewayEffectPayload(
                GatewayEffectPayload.EFFECT_NETHER_GATE_WARMUP,
                phase,
                sourceEntityId,
                center,
                gameTime,
                progress,
                (int) PortalWarmupTracker.TELEPORT_WARMUP_TICKS
            )
        );
    }

    private static final class WarmupEffectState {
        private final PortalRegistry.PortalLocation portalLocation;
        private final int sourceEntityId;
        private Vec3 lastCenter;
        private long lastSyncTick;

        private WarmupEffectState(PortalRegistry.PortalLocation portalLocation, int sourceEntityId, Vec3 lastCenter, long lastSyncTick) {
            this.portalLocation = portalLocation;
            this.sourceEntityId = sourceEntityId;
            this.lastCenter = lastCenter;
            this.lastSyncTick = lastSyncTick;
        }
    }
}

