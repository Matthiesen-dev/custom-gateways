package dev.matthiesen.custom_gateways.common;

import dev.matthiesen.common.matthiesen_lib.abstracts.AbstractCommonClientMod;
import dev.matthiesen.custom_gateways.common.client.renderer.block.PortalFrameBlockRenderer;
import dev.matthiesen.custom_gateways.common.client.renderer.item.PortalFrameItemRenderer;
import dev.matthiesen.custom_gateways.common.item.PortalFrameItem;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.registry.ItemRegistry;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class CustomGatewaysCommonClient extends AbstractCommonClientMod {
    public static final CustomGatewaysCommonClient INSTANCE = new CustomGatewaysCommonClient();

    private CustomGatewaysCommonClient() {
        super(CustomGatewaysCommon.INSTANCE);
    }

    @Override
    public void initialize() {
        CustomGatewaysCommonClient.INSTANCE.createInfoLog("Initialized Common Client");
    }

    public void registerRenderers() {
        CustomGatewaysCommonClient.INSTANCE.createInfoLog("Registering Renderers");

        ItemRegistry.PORTAL_FRAME.get().renderProviderHolder.setValue(makeRendererProvider(new PortalFrameItemRenderer()));

        INSTANCE.registerEntityRenderers(registry ->
                registry.registerBlockEntityRenderer(BlockEntityRegistry.PORTAL_FRAME_BE.get(), context -> new PortalFrameBlockRenderer())
        );
    }

    private static <T extends PortalFrameItem> GeoRenderProvider makeRendererProvider(GeoItemRenderer<T> renderer) {
        return new GeoRenderProvider() {
            private BlockEntityWithoutLevelRenderer itemRenderer;

            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = renderer;
                }
                return this.itemRenderer;
            }
        };
    }
}
