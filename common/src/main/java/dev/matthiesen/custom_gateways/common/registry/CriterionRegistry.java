package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.common.matthiesen_lib.registry.AbstractCriteriaTriggerRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.advancements.LinkCrossDimensionPortalsCriterion;
import dev.matthiesen.custom_gateways.common.advancements.LinkPortalsCriterion;
import dev.matthiesen.custom_gateways.common.advancements.UsePortalCriterion;

import java.util.function.Supplier;

public final class CriterionRegistry extends AbstractCriteriaTriggerRegistry {
    private static final CriterionRegistry INSTANCE = new CriterionRegistry();

    private CriterionRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<UsePortalCriterion> USE_PORTAL;
    public static final Supplier<LinkPortalsCriterion> LINK_PORTALS;
    public static final Supplier<LinkCrossDimensionPortalsCriterion> LINK_CROSS_DIMENSION_PORTALS;

    static {
        USE_PORTAL = INSTANCE.register("use_portal", UsePortalCriterion::new);
        LINK_PORTALS = INSTANCE.register("link_portals", LinkPortalsCriterion::new);
        LINK_CROSS_DIMENSION_PORTALS = INSTANCE.register("link_cross_dimension_portals", LinkCrossDimensionPortalsCriterion::new);
    }
}
