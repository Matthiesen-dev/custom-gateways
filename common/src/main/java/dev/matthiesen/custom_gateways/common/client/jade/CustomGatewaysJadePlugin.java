package dev.matthiesen.custom_gateways.common.client.jade;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.block.PortalFrameBlock;
import dev.matthiesen.custom_gateways.common.block.PortalPadBlock;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.block.entity.PortalPadEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;

@WailaPlugin
public final class CustomGatewaysJadePlugin implements IWailaPlugin {
    public static final ResourceLocation PORTAL_FRAME = CustomGatewaysCommon.modResource("portal_frame");
    public static final ResourceLocation PORTAL_PAD = CustomGatewaysCommon.modResource("portal_pad");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(PortalFrameJadeProvider.INSTANCE, PortalFrameEntity.class);
        registration.registerBlockDataProvider(PortalPadJadeProvider.INSTANCE, PortalPadEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(PortalFrameJadeProvider.INSTANCE, PortalFrameBlock.class);
        registration.registerBlockComponent(PortalPadJadeProvider.INSTANCE, PortalPadBlock.class);

        registration.registerBlockIcon(PortalFrameJadeProvider.INSTANCE, PortalFrameBlock.class);
        registration.registerBlockIcon(PortalPadJadeProvider.INSTANCE, PortalPadBlock.class);

        registration.addRayTraceCallback(((hitResult, accessor, originalAccessor) -> {
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
