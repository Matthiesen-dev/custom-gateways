package dev.matthiesen.custom_gateways.common.client.jade;

import dev.matthiesen.custom_gateways.common.block.PortalFrameBlock;
import net.minecraft.nbt.CompoundTag;
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

    }

    @Override
    public ResourceLocation getUid() {
        return CustomGatewaysJadePlugin.PORTAL_FRAME;
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {

    }
}
