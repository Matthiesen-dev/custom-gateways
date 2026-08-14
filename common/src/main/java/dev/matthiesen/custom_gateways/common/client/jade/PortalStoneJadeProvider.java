package dev.matthiesen.custom_gateways.common.client.jade;

import dev.matthiesen.custom_gateways.common.block.PortalStoneBlock;
import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

public enum PortalStoneJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        return currentIcon;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        tooltip.add(Component.translatable("tooltip.custom_gateways.portal_stone.destination_only").withStyle(ChatFormatting.GOLD));

        if (accessor.getServerData().contains("IsLinked") && accessor.getServerData().getBoolean("IsLinked")) {
            if (accessor.getServerData().contains("LinkedDimension") && accessor.getServerData().contains("LinkedPosition")) {
                tooltip.add(Component.empty());

                Component dimComponent = Component.literal(getWorldName(accessor.getServerData().getString("LinkedDimension")))
                    .withStyle(ChatFormatting.YELLOW);
                tooltip.add(Component.translatable("tooltip.custom_gateways.portal_frame.linked.dimension", dimComponent)
                    .withStyle(ChatFormatting.AQUA));

                Component positionComponent = Component.literal(accessor.getServerData().getString("LinkedPosition"))
                    .withStyle(ChatFormatting.YELLOW);
                tooltip.add(Component.translatable("tooltip.custom_gateways.portal_frame.linked.position", positionComponent)
                    .withStyle(ChatFormatting.AQUA));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.custom_gateways.portal_frame.unlinked").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor accessor) {
        if (!(accessor.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(accessor.getBlock() instanceof PortalStoneBlock portalStoneBlock)) {
            return;
        }

        BlockPos basePos = portalStoneBlock.getMasterPos(accessor.getLevel(), accessor.getPosition());
        PortalRegistry registry = PortalRegistry.get(serverLevel);
        PortalRegistry.PortalLocation currentPortal =
            new PortalRegistry.PortalLocation(serverLevel.dimension().location(), basePos);
        PortalRegistry.PortalLocation linkedPortal = registry.getLinkedPortal(currentPortal);

        compoundTag.putBoolean("IsLinked", linkedPortal != null);
        if (linkedPortal != null) {
            compoundTag.putString("LinkedDimension", linkedPortal.dimension().toString());
            compoundTag.putString("LinkedPosition", linkedPortal.getBlockPos().toShortString());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return CustomGatewaysJadePlugin.PORTAL_STONE;
    }

    private String getWorldName(String resource) {
        ResourceLocation resourceLocation = ResourceLocation.parse(resource);
        if (resourceLocation.getNamespace().equals("minecraft")) {
            return resourceLocation.getPath();
        }
        return resourceLocation.toString();
    }
}

