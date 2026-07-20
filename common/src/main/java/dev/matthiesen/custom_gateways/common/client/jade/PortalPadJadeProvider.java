package dev.matthiesen.custom_gateways.common.client.jade;

import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

public enum PortalPadJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        return currentIcon;
    }

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        iTooltip.add(Component.translatable("tooltip.custom_gateways.portal_pad.destination_only").withStyle(ChatFormatting.GOLD));

        if (blockAccessor.getServerData().contains("IsLinked") && blockAccessor.getServerData().getBoolean("IsLinked")) {
            if (blockAccessor.getServerData().contains("LinkedDimension") && blockAccessor.getServerData().contains("LinkedPosition")) {
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
            iTooltip.add(Component.translatable("tooltip.custom_gateways.portal_frame.unlinked").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (!(blockAccessor.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        PortalRegistry registry = PortalRegistry.get(serverLevel);
        PortalRegistry.PortalLocation currentPortal =
            new PortalRegistry.PortalLocation(serverLevel.dimension().location(), blockAccessor.getPosition());
        PortalRegistry.PortalLocation linkedPortal = registry.getLinkedPortal(currentPortal);

        compoundTag.putBoolean("IsLinked", linkedPortal != null);
        if (linkedPortal != null) {
            compoundTag.putString("LinkedDimension", linkedPortal.dimension().toString());
            compoundTag.putString("LinkedPosition", linkedPortal.getBlockPos().toShortString());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return CustomGatewaysJadePlugin.PORTAL_PAD;
    }

    private String getWorldName(String resource) {
        ResourceLocation resourceLocation = ResourceLocation.parse(resource);
        if (resourceLocation.getNamespace().equals("minecraft")) {
            return resourceLocation.getPath();
        } else {
            return resourceLocation.toString();
        }
    }
}


