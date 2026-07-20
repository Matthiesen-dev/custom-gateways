package dev.matthiesen.custom_gateways.common.client.jade;

import dev.matthiesen.custom_gateways.common.block.PortalFrameBlock;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import net.minecraft.ChatFormatting;
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

    private String getWorldName(String resource) {
        ResourceLocation resourceLocation = ResourceLocation.parse(resource);
        if (resourceLocation.getNamespace().equals("minecraft")) {
            return resourceLocation.getPath();
        } else {
            return resourceLocation.toString();
        }
    }

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("IsLinked") && blockAccessor.getServerData().getBoolean("IsLinked")) {
            if (blockAccessor.getServerData().contains("LinkedDimension") && blockAccessor.getServerData().contains("LinkedPosition")) {
                iTooltip.add(Component.translatable("tooltip.custom_gateways.portal_frame.linked")
                        .withStyle(ChatFormatting.GREEN));

                iTooltip.add(Component.empty());

                Component dimComponent = Component.literal(getWorldName(blockAccessor.getServerData().getString("LinkedDimension")))
                        .withStyle(ChatFormatting.YELLOW);
                iTooltip.add(Component.translatable("tooltip.custom_gateways.portal_frame.linked.dimension", dimComponent)
                        .withStyle(ChatFormatting.AQUA));

                Component positionComponent = Component.literal(blockAccessor.getServerData().getString("LinkedPosition"))
                        .withStyle(ChatFormatting.YELLOW);
                iTooltip.add(Component.translatable("tooltip.custom_gateways.portal_frame.linked.position", positionComponent)
                        .withStyle(ChatFormatting.AQUA));
            }
        } else {
            iTooltip.add(
                    Component.translatable("tooltip.custom_gateways.portal_frame.unlinked").withStyle(ChatFormatting.RED)
            );
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        PortalFrameEntity portalFrameEntity = (PortalFrameEntity) blockAccessor.getBlockEntity();
        PortalFrameEntity masterPortalFrameEntity = portalFrameEntity.getMasterEntity(blockAccessor.getLevel(), blockAccessor.getPosition());
        if (masterPortalFrameEntity == null) return;
        compoundTag.putBoolean("IsLinked", masterPortalFrameEntity.isLinked());
        compoundTag.putString("LinkedDimension", masterPortalFrameEntity.getLinkedDimension().toString());
        compoundTag.putString("LinkedPosition", masterPortalFrameEntity.getLinkedPosition().toShortString());
    }

    @Override
    public ResourceLocation getUid() {
        return CustomGatewaysJadePlugin.PORTAL_FRAME;
    }
}
