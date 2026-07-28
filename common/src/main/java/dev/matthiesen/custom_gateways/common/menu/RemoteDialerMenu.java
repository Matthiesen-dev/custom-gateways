package dev.matthiesen.custom_gateways.common.menu;

import dev.matthiesen.custom_gateways.common.item.RemoteDialerItem;
import dev.matthiesen.custom_gateways.common.registry.MenuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class RemoteDialerMenu extends AbstractContainerMenu {
    public static final Component TITLE = Component.translatable("menu.custom_gateways.remote_dialer.title");

    private final Inventory inventory;
    private final int dialerSlot;

    public RemoteDialerMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, inventory.selected);
    }

    public RemoteDialerMenu(int containerId, Inventory inventory, int dialerSlot) {
        super(MenuRegistry.REMOTE_DIALER_MENU.get(), containerId);
        this.inventory = inventory;
        this.dialerSlot = dialerSlot;
    }

    public static void open(ServerPlayer player, int dialerSlot) {
        player.openMenu(new SimpleMenuProvider((containerId, inventory, ignoredPlayer) ->
            new RemoteDialerMenu(containerId, inventory, dialerSlot), TITLE));
    }

    public ItemStack getDialerStack() {
        if (dialerSlot < 0 || dialerSlot >= inventory.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return inventory.getItem(dialerSlot);
    }

    public int getDialerSlot() {
        return dialerSlot;
    }

    public List<RemoteDialerItem.Entry> getEntries() {
        return RemoteDialerItem.getEntries(getDialerStack());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack stack = getDialerStack();
        return !stack.isEmpty() && stack.getItem() instanceof RemoteDialerItem;
    }
}
