package dev.matthiesen.custom_gateways.neoforge;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommonClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = CustomGatewaysCommon.MOD_ID, dist = Dist.CLIENT)
public class CustomGatewaysNeoForgeClient {
    private final CustomGatewaysCommonClient INSTANCE;

    public CustomGatewaysNeoForgeClient(IEventBus modBus) {
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerRenderers);
        this.INSTANCE = CustomGatewaysCommonClient.INSTANCE;
    }

    public void clientSetup(FMLClientSetupEvent event) {
        INSTANCE.createInfoLog("Loading for NeoForge Mod Loader (Client)");
        INSTANCE.initialize();
    }

    public void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        INSTANCE.registerRenderers();
    }
}
