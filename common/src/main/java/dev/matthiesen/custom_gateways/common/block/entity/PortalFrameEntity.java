package dev.matthiesen.custom_gateways.common.block.entity;

import dev.matthiesen.custom_gateways.common.block.PortalFrameBlock;
import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
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

import java.util.List;

public final class PortalFrameEntity extends BlockEntity implements GeoBlockEntity, DimensionalGate {
    private static final long NEARBY_PLAYER_CHECK_INTERVAL_TICKS = 20L;
    private static final PortalWarmupTracker WARMUP_TRACKER = new PortalWarmupTracker();
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
            .thenLoop("animation.portal_frame.idle");
    private static final RawAnimation LINKED_ANIM = RawAnimation.begin()
            .thenLoop("animation.portal_frame.linked");
    private static final RawAnimation LINK_ANIM = RawAnimation.begin()
            .thenPlay("animation.portal_frame.link");

    private boolean IS_LINKED = false;
    private String DIMENSION = "minecraft:overworld";
    private int X = 0;
    private int Y = 0;
    private int Z = 0;
    private long lastNearbyPlayerCheckTick = Long.MIN_VALUE;
    private boolean hasNearbyPlayerCache = false;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PortalFrameEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.PORTAL_FRAME_BE.get(), pos, state);
    }

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

    public boolean isLinked() {
        return IS_LINKED;
    }

    public boolean isSlave() {
        return getBlockState().getValue(PortalFrameBlock.IS_SLAVE);
    }

    @Override
    public ResourceLocation getLinkedDimension() {
        return ResourceLocation.parse(DIMENSION);
    }

    public BlockPos getLinkedPosition() {
        return new BlockPos(X, Y, Z);
    }

    public boolean hasNearbyPlayerCached(Level level, double targetX, double targetY, double targetZ, double maxDistanceSqr) {
        long gameTime = level.getGameTime();
        if (this.lastNearbyPlayerCheckTick == Long.MIN_VALUE
            || gameTime < this.lastNearbyPlayerCheckTick
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

        ServerLevel serverLevel = (ServerLevel) level;
        PortalRegistry.PortalLocation currentPortal =
            new PortalRegistry.PortalLocation(level.dimension().location(), blockPos);
        PortalRegistry registry = PortalRegistry.get(serverLevel);

        PortalRegistry.PortalLocation linkedPortal = registry.getLinkedPortal(currentPortal);
        if (linkedPortal == null) {
            portalFrameEntity.clearLinkedTarget();
            return;
        }

        portalFrameEntity.setLinkedTarget(linkedPortal.dimension(), linkedPortal.getBlockPos(), false);

        AABB portalBounds = new AABB(
            blockPos.getX() + 0.3, blockPos.getY() + 0.1, blockPos.getZ() + 0.3,
            blockPos.getX() + 0.7, blockPos.getY() + 1.0, blockPos.getZ() + 0.7
        );

        List<Entity> entities = level.getEntities(null, portalBounds);
        long gameTime = serverLevel.getGameTime();

        WARMUP_TRACKER.cleanupStale(gameTime);

        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, linkedPortal.dimension());
        ServerLevel targetLevel = serverLevel.getServer().getLevel(dimensionKey);
        if (targetLevel == null) return;

        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player && PlayerCooldownTracker.isOnCooldown(player)) {
                WARMUP_TRACKER.remove(player.getUUID());
                continue;
            }

            if (entity instanceof ServerPlayer player) {
                if (!WARMUP_TRACKER.processPlayer(player, currentPortal, gameTime)) {
                    continue;
                }
            }

            PortalTeleporter.teleportEntity(entity, targetLevel, linkedPortal.getBlockPos());
        }
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
}

