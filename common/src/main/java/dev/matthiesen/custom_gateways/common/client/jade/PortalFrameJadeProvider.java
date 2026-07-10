package dev.matthiesen.custom_gateways.common.client.jade;

import dev.matthiesen.custom_gateways.common.block.PortalFrameBlock;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ui.ItemStackElement;

public enum PortalFrameJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;


    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        var currentBlock = accessor.getBlock();
        if (currentBlock instanceof PortalFrameBlock portalFrameBlock) {
            var parentBlock = portalFrameBlock.getParentBlock(accessor.getLevel(), accessor.getPosition());
            if (parentBlock != null) {
                return ItemStackElement.of(parentBlock.asItem().getDefaultInstance());
            }
        }
        return currentIcon;
    }

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("LinkedDimension") && blockAccessor.getServerData().contains("LinkedPosition")) {
            iTooltip.add(
                    Component.translatable(
                            "tooltip.custom_gateways.portal_frame.linked",
                            blockAccessor.getServerData().getString("LinkedPosition"),
                            blockAccessor.getServerData().getString("LinkedDimension")
                    )
            );
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        PortalFrameEntity portalFrameEntity = (PortalFrameEntity) blockAccessor.getBlockEntity();
        PortalFrameEntity masterPortalFrameEntity = portalFrameEntity.getMasterEntity(blockAccessor.getLevel(), blockAccessor.getPosition());
        if (masterPortalFrameEntity == null) return;
        compoundTag.putString("LinkedDimension", masterPortalFrameEntity.getLinkedDimension().toString());
        compoundTag.putString("LinkedPosition", masterPortalFrameEntity.getLinkedPosition().toShortString());
    }

    @Override
    public ResourceLocation getUid() {
        return CustomGatewaysJadePlugin.PORTAL_FRAME;
    }
}
