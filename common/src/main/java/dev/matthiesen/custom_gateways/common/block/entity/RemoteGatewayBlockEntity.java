package dev.matthiesen.custom_gateways.common.block.entity;

import dev.matthiesen.custom_gateways.common.block.RemoteGatewayBlock;
import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import dev.matthiesen.custom_gateways.common.util.PlayerCooldownTracker;
import dev.matthiesen.custom_gateways.common.util.PortalTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RemoteGatewayBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final int MAX_LIFETIME_TICKS = 1200;
    private static final Map<UUID, GatewayRef> ACTIVE_BY_OWNER = new HashMap<>();

    private String destinationDimension = "minecraft:overworld";
    private int destinationX;
    private int destinationY;
    private int destinationZ;
    private UUID ownerUuid = new UUID(0L, 0L);
    private int ageTicks;

    public RemoteGatewayBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.REMOTE_GATEWAY_BE.get(), pos, state);
    }

    public static boolean spawnGateway(ServerLevel level, BlockPos basePos, Direction facing, PortalRegistry.PortalLocation destination, UUID ownerUuid) {
        if (!canPlaceGateway(level, basePos)) {
            return false;
        }

        closeActiveForOwner(level, ownerUuid);

        BlockState baseState = BlockRegistry.REMOTE_GATEWAY.get().defaultBlockState()
            .setValue(RemoteGatewayBlock.IS_TOP, false)
            .setValue(RemoteGatewayBlock.FACING, facing);
        BlockState topState = baseState.setValue(RemoteGatewayBlock.IS_TOP, true);

        level.setBlock(basePos, baseState, 3);
        level.setBlock(basePos.above(), topState, 3);

        BlockEntity blockEntity = level.getBlockEntity(basePos);
        if (!(blockEntity instanceof RemoteGatewayBlockEntity gateway)) {
            cleanupGateway(level, basePos);
            return false;
        }

        gateway.destinationDimension = destination.dimension().toString();
        gateway.destinationX = destination.x();
        gateway.destinationY = destination.y();
        gateway.destinationZ = destination.z();
        gateway.ownerUuid = ownerUuid;
        gateway.ageTicks = 0;
        gateway.setChanged();

        ACTIVE_BY_OWNER.put(ownerUuid, new GatewayRef(level.dimension().location(), basePos));
        return true;
    }

    public static void closeActiveForOwner(ServerLevel level, UUID ownerUuid) {
        GatewayRef active = ACTIVE_BY_OWNER.get(ownerUuid);
        if (active == null) {
            return;
        }

        ServerLevel activeLevel = resolveLevel(level, active.dimension);
        if (activeLevel != null) {
            cleanupGateway(activeLevel, active.basePos);
        }
        ACTIVE_BY_OWNER.remove(ownerUuid);
    }

    private static boolean canPlaceGateway(ServerLevel level, BlockPos basePos) {
        BlockState baseState = level.getBlockState(basePos);
        BlockState topState = level.getBlockState(basePos.above());
        return baseState.canBeReplaced() && topState.canBeReplaced();
    }

    private static void cleanupGateway(ServerLevel level, BlockPos basePos) {
        if (level.getBlockState(basePos).is(BlockRegistry.REMOTE_GATEWAY.get())) {
            level.removeBlock(basePos, false);
        }
        BlockPos topPos = basePos.above();
        if (level.getBlockState(topPos).is(BlockRegistry.REMOTE_GATEWAY.get())) {
            level.removeBlock(topPos, false);
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, @SuppressWarnings("unused") BlockState state, T t) {
        if (!(t instanceof RemoteGatewayBlockEntity gateway) || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        gateway.ageTicks++;
        if (gateway.ageTicks >= MAX_LIFETIME_TICKS) {
            gateway.destroySelf(serverLevel);
            return;
        }

        ResourceLocation destinationDimension;
        try {
            destinationDimension = ResourceLocation.parse(gateway.destinationDimension);
        } catch (IllegalArgumentException ex) {
            gateway.destroySelf(serverLevel);
            return;
        }

        ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, destinationDimension);
        ServerLevel targetLevel = serverLevel.getServer().getLevel(targetKey);
        if (targetLevel == null) {
            return;
        }

        AABB bounds = new AABB(
            pos.getX() + 0.15,
            pos.getY(),
            pos.getZ() + 0.15,
            pos.getX() + 0.85,
            pos.getY() + 2.0,
            pos.getZ() + 0.85
        );

        List<Entity> entities = level.getEntities(null, bounds);
        for (Entity entity : entities) {
            if (entity instanceof Player player && PlayerCooldownTracker.isOnCooldown(player)) {
                continue;
            }

            BlockPos targetPos = new BlockPos(gateway.destinationX, gateway.destinationY, gateway.destinationZ);
            PortalTeleporter.teleportEntity(entity, targetLevel, targetPos);
            gateway.destroySelf(serverLevel);
            break;
        }
    }

    private void destroySelf(ServerLevel level) {
        cleanupGateway(level, this.worldPosition);
        unregisterIfActive(level, this.ownerUuid, this.worldPosition);
    }

    private static void unregisterIfActive(ServerLevel level, UUID ownerUuid, BlockPos basePos) {
        GatewayRef ref = ACTIVE_BY_OWNER.get(ownerUuid);
        if (ref == null) {
            return;
        }

        if (Objects.equals(ref.dimension, level.dimension().location()) && Objects.equals(ref.basePos, basePos)) {
            ACTIVE_BY_OWNER.remove(ownerUuid);
        }
    }

    private static @Nullable ServerLevel resolveLevel(ServerLevel currentLevel, ResourceLocation dimension) {
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimension);
        return currentLevel.getServer().getLevel(key);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("destination_dimension", this.destinationDimension);
        tag.putInt("destination_x", this.destinationX);
        tag.putInt("destination_y", this.destinationY);
        tag.putInt("destination_z", this.destinationZ);
        tag.putUUID("owner", this.ownerUuid);
        tag.putInt("age_ticks", this.ageTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.destinationDimension = tag.getString("destination_dimension");
        this.destinationX = tag.getInt("destination_x");
        this.destinationY = tag.getInt("destination_y");
        this.destinationZ = tag.getInt("destination_z");
        if (tag.hasUUID("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
        this.ageTicks = tag.getInt("age_ticks");
    }

    private static final RawAnimation DEPLOY_ANIM = RawAnimation.begin()
            .then("animation.remote_gateway.open", Animation.LoopType.PLAY_ONCE)
            .thenLoop("animation.remote_gateway.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, this::deployAnimController));
    }

    private <E extends RemoteGatewayBlockEntity> PlayState deployAnimController(final AnimationState<E> state) {
        return state.setAndContinue(DEPLOY_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private record GatewayRef(ResourceLocation dimension, BlockPos basePos) {}
}
