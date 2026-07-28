package dev.matthiesen.custom_gateways.common.network;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RemoteDialerActionPayload(int action, int slot, int entryIndex, String name) implements CustomPacketPayload {
    public static final int ACTION_SELECT = 0;
    public static final int ACTION_DELETE = 1;
    public static final int ACTION_RENAME = 2;
    public static final int ACTION_REVALIDATE = 3;

    public static final Type<RemoteDialerActionPayload> TYPE = new Type<>(CustomGatewaysCommon.modResource("remote_dialer_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteDialerActionPayload> STREAM_CODEC =
        StreamCodec.of(RemoteDialerActionPayload::encode, RemoteDialerActionPayload::decode);

    private static RemoteDialerActionPayload decode(RegistryFriendlyByteBuf buf) {
        return new RemoteDialerActionPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(64));
    }

    private static void encode(RegistryFriendlyByteBuf buf, RemoteDialerActionPayload payload) {
        buf.writeVarInt(payload.action);
        buf.writeVarInt(payload.slot);
        buf.writeVarInt(payload.entryIndex);
        buf.writeUtf(payload.name, 64);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
