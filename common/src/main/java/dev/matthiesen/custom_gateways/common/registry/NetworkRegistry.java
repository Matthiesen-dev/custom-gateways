package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.network.GatewayEffectHandler;
import dev.matthiesen.custom_gateways.common.network.GatewayEffectPayload;
import dev.matthiesen.custom_gateways.common.network.RemoteDialerActionHandler;
import dev.matthiesen.custom_gateways.common.network.RemoteDialerActionPayload;
import dev.matthiesen.matthiesen_core.common.core.network.NetworkingManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

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

        INSTANCE.registerS2C(
                GatewayEffectPayload.TYPE,
                GatewayEffectPayload.STREAM_CODEC,
                (payload, context) -> context.enqueue(() ->
                        GatewayEffectHandler.handle(payload))
        );
    }

    public static void sendToServer(CustomPacketPayload payload) {
        if (INSTANCE == null) return;
        INSTANCE.sendToServer(payload);
    }

    public static void sendToNearbyPlayers(ServerLevel level, Vec3 center, double radius, CustomPacketPayload payload) {
        if (INSTANCE == null) return;

        double radiusSqr = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center.x, center.y, center.z) <= radiusSqr) {
                INSTANCE.sendToPlayer(player, payload);
            }
        }
    }
}
