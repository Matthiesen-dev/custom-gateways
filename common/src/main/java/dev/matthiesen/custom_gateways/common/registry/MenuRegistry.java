package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.menu.GatewayMenus;
import dev.matthiesen.custom_gateways.common.menu.RemoteDialerMenu;
import dev.matthiesen.matthiesen_core.common.registry.AbstractMenuTypeRegistry;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public final class MenuRegistry extends AbstractMenuTypeRegistry {
    private static final MenuRegistry INSTANCE = new MenuRegistry();

    private MenuRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<MenuType<RemoteDialerMenu>> REMOTE_DIALER_MENU =
        INSTANCE.register("remote_dialer", GatewayMenus::remoteDialerMenu);
}
