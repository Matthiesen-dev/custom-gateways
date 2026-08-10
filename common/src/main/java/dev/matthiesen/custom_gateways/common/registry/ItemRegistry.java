package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.custom_gateways.common.item.RemoteDialerItem;
import dev.matthiesen.matthiesen_core.common.core.registry.CreativeModeTabSectionsManager;
import dev.matthiesen.matthiesen_core.common.registry.AbstractItemRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.item.PortalFrameItem;
import dev.matthiesen.custom_gateways.common.item.PortalLinkingDevice;
import dev.matthiesen.custom_gateways.common.item.PortalPadItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class ItemRegistry extends AbstractItemRegistry {
    private static final ItemRegistry INSTANCE = new ItemRegistry();

    private ItemRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<PortalFrameItem> PORTAL_FRAME;
    public static final Supplier<PortalPadItem> PORTAL_PAD;
    public static final Supplier<PortalLinkingDevice> PORTAL_LINKING_CARD;
    public static final Supplier<RemoteDialerItem> REMOTE_DIALER;

    static {
        PORTAL_FRAME = INSTANCE.register("portal_frame", PortalFrameItem::new);
        PORTAL_PAD = INSTANCE.register("portal_pad", PortalPadItem::new);
        PORTAL_LINKING_CARD = INSTANCE.register("portal_linking_device", PortalLinkingDevice::new);
        REMOTE_DIALER = INSTANCE.register("remote_dialer", RemoteDialerItem::new);
    }

    public static Supplier<ItemStack> getCreativeModeTabIcon() {
        return () -> new ItemStack(PORTAL_FRAME.get());
    }

    public static void registerCreativeModeTabItems(CreativeModeTabSectionsManager.SectionBuilder builder) {
        builder.addItemToSection(CreativeModeTabRegistry.PORTAL_FRAMES_SECTION_ID, new ItemStack(PORTAL_FRAME.get()));
        builder.addItemToSection(CreativeModeTabRegistry.PORTAL_FRAMES_SECTION_ID, new ItemStack(PORTAL_PAD.get()));
        builder.addItemToSection(CreativeModeTabRegistry.PORTAL_TOOLS_SECTION_ID, new ItemStack(PORTAL_LINKING_CARD.get()));
        builder.addItemToSection(CreativeModeTabRegistry.PORTAL_TOOLS_SECTION_ID, new ItemStack(REMOTE_DIALER.get()));
    }
}
