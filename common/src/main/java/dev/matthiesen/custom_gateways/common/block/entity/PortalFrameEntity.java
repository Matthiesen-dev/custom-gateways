package dev.matthiesen.custom_gateways.common.block.entity;

import dev.matthiesen.custom_gateways.common.block.PortalFrameBlock;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class PortalFrameEntity extends BlockEntity implements GeoBlockEntity {
    private boolean IS_LINKED = false;
    private String DIMENSION = "minecraft:overworld";
    private int X = 0;
    private int Y = 0;
    private int Z = 0;

    public void setLinkedCoords(Level level, int x, int y, int z) {
        DIMENSION = level.dimension().location().toString();
        this.X = x;
        this.Y = y;
        this.Z = z;
    }

    public void setLinkedCoords(ResourceLocation level, int x, int y, int z) {
        DIMENSION = level.toString();
        this.X = x;
        this.Y = y;
        this.Z = z;
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
        // TODO: Add ticking logic for the portal frame entity if needed
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

