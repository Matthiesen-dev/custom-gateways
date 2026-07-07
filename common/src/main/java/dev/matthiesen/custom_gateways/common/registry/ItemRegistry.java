package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.common.matthiesen_lib.core.MatthiesenLibCreativeModeTabSectionsManager;
import dev.matthiesen.common.matthiesen_lib.registry.AbstractItemRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.item.PortalFrameItem;
import dev.matthiesen.custom_gateways.common.item.PortalLinkingCard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class ItemRegistry extends AbstractItemRegistry {
    private static final ItemRegistry INSTANCE = new ItemRegistry();

    private ItemRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static final Supplier<PortalFrameItem> PORTAL_FRAME = INSTANCE.register("portal_frame", PortalFrameItem::new);
    public static final Supplier<PortalLinkingCard> PORTAL_LINKING_CARD = INSTANCE.register("portal_linking_card", PortalLinkingCard::new);

    public static Supplier<ItemStack> getCreativeModeTabIcon() {
        return () -> new ItemStack(PORTAL_FRAME.get());
    }

    public static void registerPortalFramesToCreativeModeTab(MatthiesenLibCreativeModeTabSectionsManager.SectionBuilder builder, ResourceLocation location) {
        builder.addItemToSection(location, new ItemStack(PORTAL_FRAME.get()));
    }

    public static void registerPortalToolsToCreativeModeTab(MatthiesenLibCreativeModeTabSectionsManager.SectionBuilder builder, ResourceLocation location) {
        builder.addItemToSection(location, new ItemStack(PORTAL_LINKING_CARD.get()));
    }

    public static void init() {}
}
