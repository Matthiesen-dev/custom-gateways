package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.common.matthiesen_lib.core.MatthiesenLibCreativeModeTabSectionsManager;
import dev.matthiesen.common.matthiesen_lib.registry.AbstractItemRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.item.PortalFrameItem;
import dev.matthiesen.custom_gateways.common.item.PortalLinkingCard;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class ItemRegistry extends AbstractItemRegistry {
    private static final ItemRegistry INSTANCE = new ItemRegistry();

    private ItemRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<PortalFrameItem> PORTAL_FRAME;
    public static final Supplier<PortalLinkingCard> PORTAL_LINKING_CARD;

    static {
        PORTAL_FRAME = INSTANCE.register("portal_frame", PortalFrameItem::new);
        PORTAL_LINKING_CARD = INSTANCE.register("portal_linking_card", PortalLinkingCard::new);
    }

    public static Supplier<ItemStack> getCreativeModeTabIcon() {
        return () -> new ItemStack(PORTAL_FRAME.get());
    }

    public static void registerCreativeModeTabItems(MatthiesenLibCreativeModeTabSectionsManager.SectionBuilder builder) {
        builder.addItemToSection(CreativeModeTabRegistry.PORTAL_FRAMES_SECTION_ID, new ItemStack(PORTAL_FRAME.get()));
        builder.addItemToSection(CreativeModeTabRegistry.PORTAL_TOOLS_SECTION_ID, new ItemStack(PORTAL_LINKING_CARD.get()));
    }
}
