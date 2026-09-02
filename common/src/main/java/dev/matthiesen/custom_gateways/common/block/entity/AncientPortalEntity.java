package dev.matthiesen.custom_gateways.common.block.entity;

import dev.matthiesen.custom_gateways.common.block.AncientPortalBlock;
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
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public final class AncientPortalEntity extends BlockEntity implements GeoBlockEntity, DimensionalGate, DimensionalLink {
    private static final long NEARBY_PLAYER_CHECK_INTERVAL_TICKS = 20L;
    private static final PortalWarmupTracker WARMUP_TRACKER = new PortalWarmupTracker();
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
        .thenLoop("animation.ancient_portal.idle");
    private static final RawAnimation LINKED_ANIM = RawAnimation.begin()
        .thenLoop("animation.ancient_portal.linked");
    private static final RawAnimation LINK_ANIM = RawAnimation.begin()
        .thenPlay("animation.ancient_portal.link");

    private boolean isLinked = false;
    private String dimension = "minecraft:overworld";
    private int x = 0;
    private int y = 0;
    private int z = 0;
    private long lastNearbyPlayerCheckTick = Long.MIN_VALUE;
    private boolean hasNearbyPlayerCache = false;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AncientPortalEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ANCIENT_PORTAL_BE.get(), pos, state);
    }

    @Override
    public void setLinkedTarget(ResourceLocation dimension, BlockPos targetPos, boolean triggerLinkAnimation) {
        AncientPortalEntity masterEntity = getMasterEntity();
        if (masterEntity != this && masterEntity != null) {
            masterEntity.setLinkedTarget(dimension, targetPos, triggerLinkAnimation);
            return;
        }

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

    @Override
    public void clearLinkedTarget() {
        AncientPortalEntity masterEntity = getMasterEntity();
        if (masterEntity != this && masterEntity != null) {
            masterEntity.clearLinkedTarget();
            return;
        }

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

    private void syncToClient() {
        this.setChanged();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    public boolean isLinked() {
        AncientPortalEntity masterEntity = getMasterEntity();
        return masterEntity == null || masterEntity == this ? this.isLinked : masterEntity.isLinked;
    }

    public boolean isProxy() {
        BlockState state = this.getBlockState();
        return !(state.getValue(AncientPortalBlock.PART_X) == 1 && state.getValue(AncientPortalBlock.PART_Y) == 0);
    }

    @Override
    public ResourceLocation getLinkedDimension() {
        AncientPortalEntity masterEntity = getMasterEntity();
        return ResourceLocation.parse(masterEntity == null || masterEntity == this ? this.dimension : masterEntity.dimension);
    }

    public BlockPos getLinkedPosition() {
        AncientPortalEntity masterEntity = getMasterEntity();
        if (masterEntity == null || masterEntity == this) {
            return new BlockPos(this.x, this.y, this.z);
        }
        return new BlockPos(masterEntity.x, masterEntity.y, masterEntity.z);
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
            state.setAndContinue(this.isLinked() ? LINKED_ANIM : IDLE_ANIM))
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
        this.isLinked = tag.getBoolean("is_linked");
        this.dimension = tag.getString("dimension");
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

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (!(blockEntity instanceof AncientPortalEntity ancientPortalEntity)) {
            return;
        }
        if (ancientPortalEntity.isProxy() || level.isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        PortalRegistry.PortalLocation currentPortal =
            new PortalRegistry.PortalLocation(level.dimension().location(), blockPos);
        PortalRegistry registry = PortalRegistry.get(serverLevel);

        PortalRegistry.PortalLocation linkedPortal = registry.getLinkedPortal(currentPortal);
        if (linkedPortal == null) {
            ancientPortalEntity.clearLinkedTarget();
            return;
        }

        ancientPortalEntity.setLinkedTarget(linkedPortal.dimension(), linkedPortal.getBlockPos(), false);

        AABB portalBounds = createPortalBounds(blockPos, blockState.getValue(AncientPortalBlock.FACING));
        List<Entity> entities = level.getEntities(null, portalBounds);
        long gameTime = serverLevel.getGameTime();

        WARMUP_TRACKER.cleanupStale(gameTime);

        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, linkedPortal.dimension());
        ServerLevel targetLevel = serverLevel.getServer().getLevel(dimensionKey);
        if (targetLevel == null) {
            return;
        }

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

    private static AABB createPortalBounds(BlockPos blockPos, net.minecraft.core.Direction facing) {
        double minY = blockPos.getY() + 0.12D;
        double maxY = blockPos.getY() + 2.32D;
        if (facing.getAxis() == net.minecraft.core.Direction.Axis.Z) {
            return new AABB(
                blockPos.getX() - 0.42D, minY, blockPos.getZ() + 0.18D,
                blockPos.getX() + 1.42D, maxY, blockPos.getZ() + 0.82D
            );
        }
        return new AABB(
            blockPos.getX() + 0.18D, minY, blockPos.getZ() - 0.42D,
            blockPos.getX() + 0.82D, maxY, blockPos.getZ() + 1.42D
        );
    }

    public @Nullable AncientPortalEntity getMasterEntity(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof AncientPortalBlock ancientPortalBlock)) {
            return null;
        }
        BlockPos masterPos = ancientPortalBlock.getMasterPos(pos, state);
        BlockEntity blockEntity = level.getBlockEntity(masterPos);
        if (blockEntity instanceof AncientPortalEntity masterEntity) {
            return masterEntity;
        }
        return null;
    }

    public @Nullable AncientPortalEntity getMasterEntity() {
        if (this.level == null) {
            return this;
        }
        return getMasterEntity(this.level, this.worldPosition);
    }
}

