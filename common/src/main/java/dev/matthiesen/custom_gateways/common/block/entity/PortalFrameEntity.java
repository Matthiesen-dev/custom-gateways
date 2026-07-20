package dev.matthiesen.custom_gateways.common.block.entity;

import dev.matthiesen.custom_gateways.common.block.PortalFrameBlock;
import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.util.PlayerCooldownTracker;
import dev.matthiesen.custom_gateways.common.util.PortalTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PortalFrameEntity extends BlockEntity implements GeoBlockEntity {
    private static final long TELEPORT_WARMUP_TICKS = 60L; // 3 seconds at 20 TPS
    private static final long WARMUP_STALE_TICKS = 5L;
    private static final long NEARBY_PLAYER_CHECK_INTERVAL_TICKS = 20L;
    private static final Map<UUID, PlayerWarmup> PLAYER_WARMUPS = new HashMap<>();

    private boolean IS_LINKED = false;
    private String DIMENSION = "minecraft:overworld";
    private int X = 0;
    private int Y = 0;
    private int Z = 0;
    private long lastNearbyPlayerCheckTick = Long.MIN_VALUE;
    private boolean hasNearbyPlayerCache = false;

    public void setLinkedTarget(ResourceLocation dimension, BlockPos targetPos, boolean triggerLinkAnimation) {
        boolean changed = !this.IS_LINKED
            || !this.DIMENSION.equals(dimension.toString())
            || this.X != targetPos.getX()
            || this.Y != targetPos.getY()
            || this.Z != targetPos.getZ();

        this.IS_LINKED = true;
        this.DIMENSION = dimension.toString();
        this.X = targetPos.getX();
        this.Y = targetPos.getY();
        this.Z = targetPos.getZ();

        if (changed) {
            syncToClient();
        }

        if (triggerLinkAnimation) {
            this.triggerAnim("idle", "link");
        }
    }

    public void clearLinkedTarget() {
        if (!this.IS_LINKED) {
            return;
        }

        this.IS_LINKED = false;
        this.DIMENSION = "minecraft:overworld";
        this.X = 0;
        this.Y = 0;
        this.Z = 0;
        syncToClient();
    }

    private void syncToClient() {
        this.setChanged();

        if (this.level == null || this.level.isClientSide) {
            return;
        }

        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    // Getters for portal state
    public boolean isLinked() {
        return IS_LINKED;
    }

    public ResourceLocation getLinkedDimension() {
        return ResourceLocation.parse(DIMENSION);
    }

    public BlockPos getLinkedPosition() {
        return new BlockPos(X, Y, Z);
    }

    public boolean hasNearbyPlayerCached(Level level, double targetX, double targetY, double targetZ, double maxDistanceSqr) {
        long gameTime = level.getGameTime();
        if (gameTime < this.lastNearbyPlayerCheckTick
            || gameTime - this.lastNearbyPlayerCheckTick >= NEARBY_PLAYER_CHECK_INTERVAL_TICKS) {
            this.hasNearbyPlayerCache = false;
            for (Player player : level.players()) {
                if (player.distanceToSqr(targetX, targetY, targetZ) <= maxDistanceSqr) {
                    this.hasNearbyPlayerCache = true;
                    break;
                }
            }
            this.lastNearbyPlayerCheckTick = gameTime;
        }

        return this.hasNearbyPlayerCache;
    }

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
            .thenLoop("animation.portal_frame.idle");
    private static final RawAnimation LINKED_ANIM = RawAnimation.begin()
            .thenLoop("animation.portal_frame.linked");

    private static final RawAnimation LINK_ANIM = RawAnimation.begin()
            .thenPlay("animation.portal_frame.link");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PortalFrameEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.PORTAL_FRAME_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state ->
                state.setAndContinue(this.IS_LINKED ? LINKED_ANIM : IDLE_ANIM))
                .triggerableAnim("link", LINK_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        CompoundTag tag = compoundTag.getCompound("portal_data");
        this.IS_LINKED = tag.getBoolean("is_linked");
        this.DIMENSION = tag.getString("dimension");
        this.X = tag.getInt("x");
        this.Y = tag.getInt("y");
        this.Z = tag.getInt("z");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("is_linked", this.IS_LINKED);
        tag.putString("dimension", this.DIMENSION);
        tag.putInt("x", this.X);
        tag.putInt("y", this.Y);
        tag.putInt("z", this.Z);
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

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (!(t instanceof PortalFrameEntity portalFrameEntity)) return;
        if (blockState.getValue(PortalFrameBlock.IS_SLAVE)) return;
        if (level.isClientSide) return;

        // Check for entities in the portal's collision area
        ServerLevel serverLevel = (ServerLevel) level;

        // Get the portal location
        PortalRegistry.PortalLocation currentPortal =
            new PortalRegistry.PortalLocation(level.dimension().location(), blockPos);

        // Get the portal registry to check for linked portals
        PortalRegistry registry = PortalRegistry.get(serverLevel);

        PortalRegistry.PortalLocation linkedPortal = registry.getLinkedPortal(currentPortal);
        if (linkedPortal == null) {
            portalFrameEntity.clearLinkedTarget();
            return;
        }

        // Keep BE state in sync so linked idle animation is selected.
        portalFrameEntity.setLinkedTarget(linkedPortal.dimension(), linkedPortal.getBlockPos(), false);

        // Create bounding box for the portal (center area of the frame)
        AABB portalBounds = new AABB(
            blockPos.getX() + 0.3,
            blockPos.getY() + 0.1,
            blockPos.getZ() + 0.3,
            blockPos.getX() + 0.7,
            blockPos.getY() + 1.0,
            blockPos.getZ() + 0.7
        );

        // Get all entities in the portal area
        List<Entity> entities = level.getEntities(null, portalBounds);
        long gameTime = serverLevel.getGameTime();

        cleanupStaleWarmups(gameTime);

        // Resolve target level once for all entities in this portal.
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, linkedPortal.dimension());
        ServerLevel targetLevel = serverLevel.getServer().getLevel(dimensionKey);
        if (targetLevel == null) return;

        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player && PlayerCooldownTracker.isOnCooldown(player)) {
                PLAYER_WARMUPS.remove(player.getUUID());
                continue; // Player is on cooldown
            }

            if (entity instanceof ServerPlayer player) {
                PlayerWarmup warmup = PLAYER_WARMUPS.get(player.getUUID());
                if (warmup == null || !warmup.portalLocation.equals(currentPortal)) {
                    warmup = new PlayerWarmup(currentPortal, gameTime, gameTime);
                    PLAYER_WARMUPS.put(player.getUUID(), warmup);
                } else {
                    warmup.lastSeenTick = gameTime;
                }

                long elapsedTicks = gameTime - warmup.startedTick;
                long remainingTicks = TELEPORT_WARMUP_TICKS - elapsedTicks;

                if (remainingTicks > 0L) {
                    double remainingSeconds = remainingTicks / 20.0D;
                    player.sendSystemMessage(Component.literal(String.format("Teleporting in %.1fs", remainingSeconds)), true);
                    continue;
                }

                PLAYER_WARMUPS.remove(player.getUUID());
            }

            // Teleport the entity
            PortalTeleporter.teleportEntity(entity, targetLevel, linkedPortal.getBlockPos());
        }
    }

    private static void cleanupStaleWarmups(long gameTime) {
        PLAYER_WARMUPS.entrySet().removeIf(entry ->
                gameTime - entry.getValue().lastSeenTick > WARMUP_STALE_TICKS);
    }

    public PortalFrameEntity getMasterEntity(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).getValue(PortalFrameBlock.IS_SLAVE)) {
            return this;
        }
        BlockPos masterPos = pos.below();
        BlockEntity blockEntity = level.getBlockEntity(masterPos);
        if (blockEntity instanceof PortalFrameEntity masterEntity) {
            return masterEntity;
        }
        return null;
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

