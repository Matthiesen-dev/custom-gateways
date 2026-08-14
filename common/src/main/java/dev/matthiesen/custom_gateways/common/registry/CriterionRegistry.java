package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.custom_gateways.common.advancements.SimpleTriggerCriterion;
import dev.matthiesen.matthiesen_core.common.registry.AbstractCriteriaTriggerRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;

import java.util.function.Supplier;

public final class CriterionRegistry extends AbstractCriteriaTriggerRegistry {
    private static final CriterionRegistry INSTANCE = new CriterionRegistry();

    private CriterionRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<SimpleTriggerCriterion> USE_PORTAL;
    public static final Supplier<SimpleTriggerCriterion> LINK_PORTALS;
    public static final Supplier<SimpleTriggerCriterion> LINK_CROSS_DIMENSION_PORTALS;
    public static final Supplier<SimpleTriggerCriterion> USE_REMOTE_DIALER;

    static {
        USE_PORTAL = INSTANCE.register("use_portal", SimpleTriggerCriterion::new);
        LINK_PORTALS = INSTANCE.register("link_portals", SimpleTriggerCriterion::new);
        LINK_CROSS_DIMENSION_PORTALS = INSTANCE.register("link_cross_dimension_portals", SimpleTriggerCriterion::new);
        USE_REMOTE_DIALER = INSTANCE.register("use_remote_dialer", SimpleTriggerCriterion::new);
    }
}
