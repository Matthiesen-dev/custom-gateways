package dev.matthiesen.custom_gateways.common.util;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class PortalLinkTags {
    public static final TagKey<Block> PORTAL_LINK_SOURCES =
        TagKey.create(Registries.BLOCK, CustomGatewaysCommon.modResource("portal_link_sources"));
    public static final TagKey<Block> PORTAL_LINK_DESTINATIONS =
        TagKey.create(Registries.BLOCK, CustomGatewaysCommon.modResource("portal_link_destinations"));

    private PortalLinkTags() {
    }
}

