package dev.matthiesen.custom_gateways.common.network;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record GatewayEffectPayload(
    int effectType,
    int phase,
    int sourceEntityId,
    double centerX,
    double centerY,
    double centerZ,
    long serverGameTime,
    float progress,
    int durationTicks
) implements CustomPacketPayload {
    public static final int EFFECT_NETHER_GATE_WARMUP = 0;

    public static final int PHASE_START = 0;
    public static final int PHASE_SYNC = 1;
    public static final int PHASE_STOP = 2;

    public static final Type<GatewayEffectPayload> TYPE = new Type<>(CustomGatewaysCommon.modResource("gateway_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GatewayEffectPayload> STREAM_CODEC =
        StreamCodec.of(GatewayEffectPayload::encode, GatewayEffectPayload::decode);

    public GatewayEffectPayload(int effectType, int phase, int sourceEntityId, Vec3 center, long serverGameTime, float progress, int durationTicks) {
        this(effectType, phase, sourceEntityId, center.x, center.y, center.z, serverGameTime, progress, durationTicks);
    }

    public Vec3 center() {
        return new Vec3(this.centerX, this.centerY, this.centerZ);
    }

    private static GatewayEffectPayload decode(RegistryFriendlyByteBuf buf) {
        return new GatewayEffectPayload(
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readLong(),
            buf.readFloat(),
            buf.readVarInt()
        );
    }

    private static void encode(RegistryFriendlyByteBuf buf, GatewayEffectPayload payload) {
        buf.writeVarInt(payload.effectType);
        buf.writeVarInt(payload.phase);
        buf.writeVarInt(payload.sourceEntityId);
        buf.writeDouble(payload.centerX);
        buf.writeDouble(payload.centerY);
        buf.writeDouble(payload.centerZ);
        buf.writeLong(payload.serverGameTime);
        buf.writeFloat(payload.progress);
        buf.writeVarInt(payload.durationTicks);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

