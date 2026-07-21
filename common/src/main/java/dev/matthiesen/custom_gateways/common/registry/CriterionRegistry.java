package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.common.matthiesen_lib.registry.AbstractCriteriaTriggerRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.advancements.UsePortalCriterion;

import java.util.function.Supplier;

public final class CriterionRegistry extends AbstractCriteriaTriggerRegistry {
    private static final CriterionRegistry INSTANCE = new CriterionRegistry();

    private CriterionRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<UsePortalCriterion> USE_PORTAL;

    static {
        USE_PORTAL = INSTANCE.register("use_portal", UsePortalCriterion::new);
    }
}
