package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.matthiesen_core.common.registry.AbstractCreativeModeTabRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;

public final class CreativeModeTabRegistry extends AbstractCreativeModeTabRegistry {
    private static final CreativeModeTabRegistry INSTANCE = new CreativeModeTabRegistry();

    private CreativeModeTabRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final ResourceLocation PORTAL_FRAME_TAB_ID = CustomGatewaysCommon.modResource("portal_frame_tab");
    public static final ResourceLocation PORTAL_FRAMES_SECTION_ID = CustomGatewaysCommon.modResource("portal_frames_section");
    public static final ResourceLocation PORTAL_TOOLS_SECTION_ID = CustomGatewaysCommon.modResource("portal_tools_section");

    public static final Supplier<CreativeModeTab> PORTAL_FRAMES_TAB;

    static {
        PORTAL_FRAMES_TAB = INSTANCE.registerSectionedCreativeTab(
                PORTAL_FRAME_TAB_ID,
                Component.translatable("itemGroup.custom_gateways.portal_frame_tab"),
                ItemRegistry.getCreativeModeTabIcon(),
                sectionBuilder -> {
                    sectionBuilder.registerSection(
                            PORTAL_FRAMES_SECTION_ID,
                            Component.translatable("itemGroup.custom_gateways.portal_frame_tab.portal_frames_section"),
                            100
                    );
                    sectionBuilder.registerSection(
                            PORTAL_TOOLS_SECTION_ID,
                            Component.translatable("itemGroup.custom_gateways.portal_frame_tab.portal_tools_section"),
                            50
                    );

                    ItemRegistry.registerCreativeModeTabItems(sectionBuilder);
                }
        );
    }
}
