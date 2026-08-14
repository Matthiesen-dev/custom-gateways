package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.matthiesen_core.common.registry.AbstractSoundRegistry;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public final class SoundRegistry extends AbstractSoundRegistry {
    private static final SoundRegistry INSTANCE = new SoundRegistry();

    private SoundRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<SoundEvent> GATEWAY_TELEPORT_SUCCESS;
    public static final Supplier<SoundEvent> GATEWAY_TELEPORT_FAILURE;
    public static final Supplier<SoundEvent> LINKING_DEVICE_SET_SOURCE;
    public static final Supplier<SoundEvent> LINKING_DEVICE_LINK_PORTAL;
    public static final Supplier<SoundEvent> LINKING_DEVICE_UNLINK_PORTAL;

    static {
        GATEWAY_TELEPORT_SUCCESS = INSTANCE.register("gateway_teleport_success", create("gateway.teleport.success"));
        GATEWAY_TELEPORT_FAILURE = INSTANCE.register("gateway_teleport_failure", create("gateway.teleport.failure"));
        LINKING_DEVICE_SET_SOURCE = INSTANCE.register("linking_card_set_source", create("linking_device.set_source"));
        LINKING_DEVICE_LINK_PORTAL = INSTANCE.register("linking_device_link_portal", create("linking_device.link_portal"));
        LINKING_DEVICE_UNLINK_PORTAL = INSTANCE.register("linking_device_unlink_portal", create("linking_device.unlink_portal"));
    }

    public static Supplier<SoundEvent> create(String name) {
        return () -> SoundEvent.createVariableRangeEvent(CustomGatewaysCommon.modResource(name));
    }
}
