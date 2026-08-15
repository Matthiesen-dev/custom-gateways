package dev.matthiesen.custom_gateways.common.block.entity;

import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.util.PlayerCooldownTracker;
import dev.matthiesen.custom_gateways.common.util.PortalTeleporter;
import dev.matthiesen.custom_gateways.common.util.PortalWarmupTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
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

public final class NetherGateEntity extends BlockEntity implements GeoBlockEntity, DimensionalGate {
    private static final PortalWarmupTracker WARMUP_TRACKER = new PortalWarmupTracker();

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
        if (targetLevel == null) return;

        for (Entity entityInBounds : entities) {
            if (entityInBounds instanceof ServerPlayer player && PlayerCooldownTracker.isOnCooldown(player)) {
                WARMUP_TRACKER.remove(player.getUUID());
                continue;
            }

            if (entityInBounds instanceof ServerPlayer player) {
                if (!WARMUP_TRACKER.processPlayer(player, currentPortal, gameTime)) {
                    // Still in warmup — emit the fire spiral around this player
                    float progress = WARMUP_TRACKER.getWarmupProgress(player.getUUID(), gameTime);
                    spawnWarmupSpiralParticles(serverLevel, player, gameTime, progress);
                    continue;
                }
            }

            PortalTeleporter.teleportEntity(entityInBounds, targetLevel, linkedPortal.getBlockPos());
        }
    }

    /**
     * Emits a spiralling fire helix around the player during the teleport warmup countdown.
     * Two staggered rings of FLAME + SOUL_FIRE_FLAME particles rotate and rise around the player.
     * The radius tightens as the countdown nears completion.
     */
    private static void spawnWarmupSpiralParticles(ServerLevel level, Player player, long gameTime, float progress) {
        double baseAngle = (gameTime * 0.25D) % (2.0D * Math.PI);
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        // Radius tightens from 0.7 → 0.4 as the countdown finishes (pulls inward)
        double radius = 0.7D - progress * 0.3D;
        int rings = 2;
        int particlesPerRing = 5;
        double ringSpacing = 0.5D;

        for (int ring = 0; ring < rings; ring++) {
            // Each ring scrolls upward independently, looping back to feet every ~2.2 blocks
            double heightOffset = ((gameTime * 0.08D + ring * ringSpacing) % 2.2D);
            for (int i = 0; i < particlesPerRing; i++) {
                double angle = baseAngle + (2.0D * Math.PI * i / particlesPerRing) + ring * 0.63D;
                double particleX = px + Math.cos(angle) * radius;
                double particleY = py + heightOffset;
                double particleZ = pz + Math.sin(angle) * radius;

                // Alternate between warm flame and blue soul-fire for a nether aesthetic
                if ((ring + i) % 2 == 0) {
                    level.sendParticles(ParticleTypes.FLAME,
                        particleX, particleY, particleZ, 0, 0.0D, 1.0D, 0.0D, 0.05D);
                } else {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        particleX, particleY, particleZ, 0, 0.0D, 1.0D, 0.0D, 0.04D);
                }
            }
        }
    }
}

