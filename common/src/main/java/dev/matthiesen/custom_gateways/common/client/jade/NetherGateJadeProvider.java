package dev.matthiesen.custom_gateways.common.client.jade;

import dev.matthiesen.custom_gateways.common.block.entity.NetherGateEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

public enum NetherGateJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        return currentIcon;
    }

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("IsLinked") && blockAccessor.getServerData().getBoolean("IsLinked")) {
            iTooltip.add(Component.translatable("tooltip.custom_gateways.nether_gate.linked")
                .withStyle(ChatFormatting.GREEN));
            iTooltip.add(Component.empty());

            Component dimComponent = Component.literal(
                    getWorldName(blockAccessor.getServerData().getString("LinkedDimension")))
                .withStyle(ChatFormatting.YELLOW);
            iTooltip.add(Component.translatable("tooltip.custom_gateways.portal_frame.linked.dimension", dimComponent)
                .withStyle(ChatFormatting.AQUA));

            Component positionComponent = Component.literal(
                    blockAccessor.getServerData().getString("LinkedPosition"))
                .withStyle(ChatFormatting.YELLOW);
            iTooltip.add(Component.translatable("tooltip.custom_gateways.portal_frame.linked.position", positionComponent)
                .withStyle(ChatFormatting.AQUA));
        } else {
            iTooltip.add(Component.translatable("tooltip.custom_gateways.nether_gate.unlinked")
                .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (!(blockAccessor.getBlockEntity() instanceof NetherGateEntity entity)) {
            return;
        }
        compoundTag.putBoolean("IsLinked", entity.isLinked());
        compoundTag.putString("LinkedDimension", entity.getLinkedDimension().toString());
        compoundTag.putString("LinkedPosition", entity.getLinkedPosition().toShortString());
    }

    @Override
    public ResourceLocation getUid() {
        return CustomGatewaysJadePlugin.NETHER_GATE;
    }

    private String getWorldName(String resource) {
        ResourceLocation resourceLocation = ResourceLocation.parse(resource);
        return resourceLocation.getNamespace().equals("minecraft")
            ? resourceLocation.getPath()
            : resourceLocation.toString();
    }
}

