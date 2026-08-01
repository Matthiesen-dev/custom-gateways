package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.matthiesen_core.common.registry.AbstractBlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.block.entity.PortalPadEntity;
import dev.matthiesen.custom_gateways.common.block.entity.RemoteGatewayBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class BlockEntityRegistry extends AbstractBlockEntityRegistry {
    private static final BlockEntityRegistry INSTANCE = new BlockEntityRegistry();

    private BlockEntityRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<BlockEntityType<PortalFrameEntity>> PORTAL_FRAME_BE;
    public static final Supplier<BlockEntityType<PortalPadEntity>> PORTAL_PAD_BE;
    public static final Supplier<BlockEntityType<RemoteGatewayBlockEntity>> REMOTE_GATEWAY_BE;

    static {
        PORTAL_FRAME_BE = INSTANCE.register("portal_frame", PortalFrameEntity::new, BlockRegistry.PORTAL_FRAME);
        PORTAL_PAD_BE = INSTANCE.register("portal_pad", PortalPadEntity::new, BlockRegistry.PORTAL_PAD);
        REMOTE_GATEWAY_BE = INSTANCE.register("remote_gateway", RemoteGatewayBlockEntity::new, BlockRegistry.REMOTE_GATEWAY);
    }
}
