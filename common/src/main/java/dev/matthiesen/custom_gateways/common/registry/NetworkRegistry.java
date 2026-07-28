package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.network.RemoteDialerActionHandler;
import dev.matthiesen.custom_gateways.common.network.RemoteDialerActionPayload;
import dev.matthiesen.matthiesen_core.common.core.network.NetworkingManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class NetworkRegistry {
    private static NetworkingManager INSTANCE;

    public static void init(CustomGatewaysCommon modInstance) {
        INSTANCE = modInstance.getNetworkingManager();

        INSTANCE.registerC2S(
            RemoteDialerActionPayload.TYPE,
            RemoteDialerActionPayload.STREAM_CODEC,
            (payload, context) -> context.enqueue(() -> {
                if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
                RemoteDialerActionHandler.handle(serverPlayer, payload);
            })
        );
    }

    public static void sendToServer(CustomPacketPayload payload) {
        if (INSTANCE == null) return;
        INSTANCE.sendToServer(payload);
    }
}
