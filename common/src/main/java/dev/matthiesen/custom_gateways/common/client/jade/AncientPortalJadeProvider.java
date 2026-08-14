package dev.matthiesen.custom_gateways.common.client.jade;

import dev.matthiesen.custom_gateways.common.block.AncientPortalBlock;
import dev.matthiesen.custom_gateways.common.block.entity.AncientPortalEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ui.ItemStackElement;

public enum AncientPortalJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        var currentBlock = accessor.getBlock();
        if (currentBlock instanceof AncientPortalBlock ancientPortalBlock) {
            var parentBlock = ancientPortalBlock.getParentBlock(accessor.getLevel(), accessor.getPosition());
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
        }
        return resourceLocation.toString();
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("IsLinked") && accessor.getServerData().getBoolean("IsLinked")) {
            if (accessor.getServerData().contains("LinkedDimension") && accessor.getServerData().contains("LinkedPosition")) {
                tooltip.add(Component.translatable("tooltip.custom_gateways.ancient_portal.linked")
                    .withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.empty());

                Component dimComponent = Component.literal(getWorldName(accessor.getServerData().getString("LinkedDimension")))
                    .withStyle(ChatFormatting.YELLOW);
                tooltip.add(Component.translatable("tooltip.custom_gateways.ancient_portal.linked.dimension", dimComponent)
                    .withStyle(ChatFormatting.AQUA));

                Component positionComponent = Component.literal(accessor.getServerData().getString("LinkedPosition"))
                    .withStyle(ChatFormatting.YELLOW);
                tooltip.add(Component.translatable("tooltip.custom_gateways.ancient_portal.linked.position", positionComponent)
                    .withStyle(ChatFormatting.AQUA));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.custom_gateways.ancient_portal.unlinked").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor accessor) {
        if (!(accessor.getBlock() instanceof AncientPortalBlock ancientPortalBlock)) {
            return;
        }

        BlockPos masterPos = ancientPortalBlock.getMasterPos(accessor.getLevel(), accessor.getPosition());
        BlockEntity blockEntity = accessor.getLevel().getBlockEntity(masterPos);
        if (!(blockEntity instanceof AncientPortalEntity masterEntity)) {
            return;
        }

        compoundTag.putBoolean("IsLinked", masterEntity.isLinked());
        compoundTag.putString("LinkedDimension", masterEntity.getLinkedDimension().toString());
        compoundTag.putString("LinkedPosition", masterEntity.getLinkedPosition().toShortString());
    }

    @Override
    public ResourceLocation getUid() {
        return CustomGatewaysJadePlugin.ANCIENT_PORTAL;
    }
}



