package dev.matthiesen.custom_gateways.neoforge;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommonClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CustomGatewaysCommon.MOD_ID, value = Dist.CLIENT)
public class CustomGatewaysNeoForgeClientEventBus {
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        var INSTANCE = CustomGatewaysCommonClient.INSTANCE;
        INSTANCE.registerRenderers();
    }
}
