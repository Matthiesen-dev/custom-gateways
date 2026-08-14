package dev.matthiesen.custom_gateways.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Base class for the simple portal block entities ({@link PortalPadEntity},
 * {@link PortalStoneEntity}) that only need to track a single linked/unlinked flag.
 *
 * <p>Provides:
 * <ul>
 *   <li>The {@code isLinked} field with {@link #isLinked()} / {@link #setLinked(boolean)}</li>
 *   <li>Client-sync via {@link #syncToClient()}</li>
 *   <li>NBT save/load for the linked flag</li>
 *   <li>Standard {@link #getUpdatePacket()} / {@link #getUpdateTag(HolderLookup.Provider)}</li>
 *   <li>A shared {@link AnimatableInstanceCache} for GeckoLib</li>
 * </ul>
 * Subclasses must implement {@link #registerControllers} to supply their animations.
 */
public abstract class AbstractLinkedBlockEntity extends BlockEntity implements GeoBlockEntity {
    protected static final String LINKED_TAG = "is_linked";
    protected static final long LINK_STATE_CHECK_INTERVAL_TICKS = 20L;

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private boolean isLinked = false;

    protected AbstractLinkedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ------------------------------------------------------------------ state

    public boolean isLinked() {
        return this.isLinked;
    }

    public void setLinked(boolean linked) {
        if (this.isLinked == linked) {
            return;
        }
        this.isLinked = linked;
        syncToClient();
    }

    // ------------------------------------------------------------------ sync

    protected void syncToClient() {
        this.setChanged();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    // ------------------------------------------------------------------ NBT

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.isLinked = tag.getBoolean(LINKED_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean(LINKED_TAG, this.isLinked);
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
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }
}

