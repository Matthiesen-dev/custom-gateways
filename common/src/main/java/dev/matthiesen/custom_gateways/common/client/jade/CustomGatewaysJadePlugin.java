package dev.matthiesen.custom_gateways.common.client.jade;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.block.AncientPortalBlock;
import dev.matthiesen.custom_gateways.common.block.PortalFrameBlock;
import dev.matthiesen.custom_gateways.common.block.PortalPadBlock;
import dev.matthiesen.custom_gateways.common.block.entity.AncientPortalEntity;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.block.entity.PortalPadEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;

@WailaPlugin
public final class CustomGatewaysJadePlugin implements IWailaPlugin {
    public static final ResourceLocation ANCIENT_PORTAL = CustomGatewaysCommon.modResource("ancient_portal");
    public static final ResourceLocation PORTAL_FRAME = CustomGatewaysCommon.modResource("portal_frame");
    public static final ResourceLocation PORTAL_PAD = CustomGatewaysCommon.modResource("portal_pad");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(AncientPortalJadeProvider.INSTANCE, AncientPortalEntity.class);
        registration.registerBlockDataProvider(AncientPortalJadeProvider.INSTANCE, AncientPortalBlock.class);
        registration.registerBlockDataProvider(PortalFrameJadeProvider.INSTANCE, PortalFrameEntity.class);
        registration.registerBlockDataProvider(PortalPadJadeProvider.INSTANCE, PortalPadEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(AncientPortalJadeProvider.INSTANCE, AncientPortalBlock.class);
        registration.registerBlockComponent(PortalFrameJadeProvider.INSTANCE, PortalFrameBlock.class);
        registration.registerBlockComponent(PortalPadJadeProvider.INSTANCE, PortalPadBlock.class);

        registration.registerBlockIcon(AncientPortalJadeProvider.INSTANCE, AncientPortalBlock.class);
        registration.registerBlockIcon(PortalFrameJadeProvider.INSTANCE, PortalFrameBlock.class);
        registration.registerBlockIcon(PortalPadJadeProvider.INSTANCE, PortalPadBlock.class);

        registration.addRayTraceCallback(((hitResult, accessor, originalAccessor) -> {
            if (accessor instanceof BlockAccessor blockAccessor && blockAccessor.getBlock() instanceof AncientPortalBlock blockTemplate) {
                return registration.blockAccessor()
                        .from(blockAccessor)
                        .blockState(blockTemplate.getParentBlockState(blockAccessor.getLevel(), blockAccessor.getPosition()))
                        .build();
            }
            if (accessor instanceof BlockAccessor blockAccessor && blockAccessor.getBlock() instanceof PortalFrameBlock blockTemplate) {
                return registration.blockAccessor()
                        .from(blockAccessor)
                        .blockState(blockTemplate.getParentBlockState(blockAccessor.getLevel(), blockAccessor.getPosition()))
                        .build();
            }
            return accessor;
        }));
    }
}
