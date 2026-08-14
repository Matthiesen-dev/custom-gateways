package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.matthiesen_core.common.registry.AbstractBlockRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.block.AncientPortalBlock;
import dev.matthiesen.custom_gateways.common.block.PortalFrameBlock;
import dev.matthiesen.custom_gateways.common.block.PortalPadBlock;
import dev.matthiesen.custom_gateways.common.block.RemoteGatewayBlock;

import java.util.function.Supplier;

public final class BlockRegistry extends AbstractBlockRegistry {
    private static final BlockRegistry INSTANCE = new BlockRegistry();

    private BlockRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<AncientPortalBlock> ANCIENT_PORTAL;
    public static final Supplier<PortalFrameBlock> PORTAL_FRAME;
    public static final Supplier<PortalPadBlock> PORTAL_PAD;
    public static final Supplier<RemoteGatewayBlock> REMOTE_GATEWAY;

    static {
        ANCIENT_PORTAL = INSTANCE.register("ancient_portal", AncientPortalBlock::new);
        PORTAL_FRAME = INSTANCE.register("portal_frame", PortalFrameBlock::new);
        PORTAL_PAD = INSTANCE.register("portal_pad", PortalPadBlock::new);
        REMOTE_GATEWAY = INSTANCE.register("remote_gateway", RemoteGatewayBlock::new);
    }
}
