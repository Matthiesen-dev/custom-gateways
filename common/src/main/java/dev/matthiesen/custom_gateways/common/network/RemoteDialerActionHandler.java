package dev.matthiesen.custom_gateways.common.network;

import dev.matthiesen.custom_gateways.common.item.RemoteDialerItem;
import dev.matthiesen.custom_gateways.common.menu.RemoteDialerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class RemoteDialerActionHandler {
    private RemoteDialerActionHandler() {
    }

    public static void handle(ServerPlayer player, RemoteDialerActionPayload payload) {
        if (!(player.containerMenu instanceof RemoteDialerMenu menu)) {
            return;
        }

        if (payload.slot() != menu.getDialerSlot()) {
            return;
        }

        ItemStack stack = menu.getDialerStack();
        if (!(stack.getItem() instanceof RemoteDialerItem)) {
            return;
        }

        switch (payload.action()) {
            case RemoteDialerActionPayload.ACTION_SELECT -> {
                boolean opened = RemoteDialerItem.openGateway(player, stack, payload.entryIndex());
                if (!opened) {
                    player.displayClientMessage(Component.translatable("interaction.custom_gateways.remote_dialer.select.failed"), true);
                }
            }
            case RemoteDialerActionPayload.ACTION_DELETE -> RemoteDialerItem.deleteEntry(stack, payload.entryIndex());
            case RemoteDialerActionPayload.ACTION_RENAME -> {
                boolean renamed = RemoteDialerItem.renameEntry(stack, payload.entryIndex(), payload.name());
                if (!renamed) {
                    player.displayClientMessage(Component.translatable("interaction.custom_gateways.remote_dialer.rename.failed"), true);
                }
            }
            case RemoteDialerActionPayload.ACTION_REVALIDATE -> RemoteDialerItem.revalidateEntries(stack, player.serverLevel());
            default -> {
            }
        }

        player.getInventory().setChanged();
    }
}

