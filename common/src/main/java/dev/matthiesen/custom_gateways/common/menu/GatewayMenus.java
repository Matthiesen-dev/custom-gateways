package dev.matthiesen.custom_gateways.common.menu;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class GatewayMenus<T extends AbstractContainerMenu> extends MenuType<T> {
    public GatewayMenus(MenuSupplier<T> menuSupplier) {
        super(menuSupplier, FeatureFlagSet.of());
    }

    public static GatewayMenus<RemoteDialerMenu> remoteDialerMenu() {
        return new GatewayMenus<>(RemoteDialerMenu::new);
    }
}
