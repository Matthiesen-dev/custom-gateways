package dev.matthiesen.custom_gateways.neoforge;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommonClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = CustomGatewaysCommon.MOD_ID, dist = Dist.CLIENT)
public class CustomGatewaysNeoForgeClient {
    public CustomGatewaysNeoForgeClient(IEventBus modBus) {
        modBus.addListener(this::clientSetup);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        CustomGatewaysCommonClient.initialize();
    }
}
