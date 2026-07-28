package dev.matthiesen.custom_gateways.common.client.screen;

import dev.matthiesen.custom_gateways.common.item.RemoteDialerItem;
import dev.matthiesen.custom_gateways.common.menu.RemoteDialerMenu;
import dev.matthiesen.custom_gateways.common.network.RemoteDialerActionPayload;
import dev.matthiesen.custom_gateways.common.registry.NetworkRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public final class RemoteDialerScreen extends AbstractContainerScreen<RemoteDialerMenu> {
    private static final int ROW_HEIGHT = 18;
    private static final int VISIBLE_ROWS = 7;

    private Button selectButton;
    private Button deleteButton;
    private Button renameButton;
    private Button revalidateButton;
    private Button scrollUpButton;
    private Button scrollDownButton;
    private EditBox renameInput;

    private int selectedIndex = -1;
    private int scrollOffset = 0;

    public RemoteDialerScreen(RemoteDialerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 245;
        this.imageHeight = 185;
    }

    @Override
    protected void init() {
        super.init();

        int left = this.leftPos;
        int top = this.topPos;

        this.renameInput = new EditBox(this.font, left + 12, top + 148, 140, 18, Component.translatable("menu.custom_gateways.remote_dialer.rename_input"));
        this.renameInput.setMaxLength(48);
        this.addRenderableWidget(this.renameInput);

        this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("menu.custom_gateways.remote_dialer.open"),
            button -> sendAction(RemoteDialerActionPayload.ACTION_SELECT, ""))
            .bounds(left + 162, top + 148, 70, 18)
            .build());

        this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("menu.custom_gateways.remote_dialer.delete"),
            button -> sendAction(RemoteDialerActionPayload.ACTION_DELETE, ""))
            .bounds(left + 162, top + 126, 70, 18)
            .build());

        this.renameButton = this.addRenderableWidget(Button.builder(Component.translatable("menu.custom_gateways.remote_dialer.rename"),
            button -> sendAction(RemoteDialerActionPayload.ACTION_RENAME, this.renameInput.getValue()))
            .bounds(left + 12, top + 170, 68, 14)
            .build());

        this.revalidateButton = this.addRenderableWidget(Button.builder(Component.translatable("menu.custom_gateways.remote_dialer.revalidate"),
            button -> sendAction(RemoteDialerActionPayload.ACTION_REVALIDATE, ""))
            .bounds(left + 84, top + 170, 68, 14)
            .build());

        this.scrollUpButton = this.addRenderableWidget(Button.builder(Component.literal("^"), button -> {
            if (scrollOffset > 0) {
                scrollOffset--;
            }
        }).bounds(left + 220, top + 18, 12, 14).build());

        this.scrollDownButton = this.addRenderableWidget(Button.builder(Component.literal("v"), button -> {
            int maxOffset = Math.max(0, menu.getEntries().size() - VISIBLE_ROWS);
            if (scrollOffset < maxOffset) {
                scrollOffset++;
            }
        }).bounds(left + 220, top + 106, 12, 14).build());

        refreshButtonStates();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        refreshButtonStates();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int listLeft = this.leftPos + 12;
        int listTop = this.topPos + 18;
        int listWidth = 204;

        if (mouseX < listLeft || mouseX > listLeft + listWidth) {
            return false;
        }

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int y = listTop + row * ROW_HEIGHT;
            if (mouseY >= y && mouseY <= y + ROW_HEIGHT - 2) {
                int idx = scrollOffset + row;
                if (idx < menu.getEntries().size()) {
                    this.selectedIndex = idx;
                    this.renameInput.setValue(menu.getEntries().get(idx).name());
                    refreshButtonStates();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xCC202020);
        graphics.fill(left + 10, top + 16, left + 218, top + 142, 0xCC111111);

        List<RemoteDialerItem.Entry> entries = menu.getEntries();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int idx = scrollOffset + row;
            if (idx >= entries.size()) {
                break;
            }

            int y = top + 18 + row * ROW_HEIGHT;
            boolean selected = idx == selectedIndex;
            int rowColor = selected ? 0xAA3A3A3A : 0xAA262626;
            graphics.fill(left + 12, y, left + 216, y + ROW_HEIGHT - 2, rowColor);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 12, 6, 0xFFFFFF, false);

        List<RemoteDialerItem.Entry> entries = menu.getEntries();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int idx = scrollOffset + row;
            if (idx >= entries.size()) {
                break;
            }

            RemoteDialerItem.Entry entry = entries.get(idx);
            int color = entry.valid() ? 0xC8FFC8 : 0xFF6666;
            String label = entry.name();
            if (!entry.valid()) {
                label = label + " (Invalid)";
            }

            graphics.drawString(this.font, label, 14, 22 + row * ROW_HEIGHT, color, false);
        }

        graphics.drawString(this.font, Component.translatable("menu.custom_gateways.remote_dialer.rename_hint"), 12, 138, 0xA0A0A0, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void sendAction(int action, String name) {
        int index = selectedIndex;
        if (action == RemoteDialerActionPayload.ACTION_REVALIDATE) {
            index = -1;
        }

        NetworkRegistry.sendToServer(new RemoteDialerActionPayload(action, menu.getDialerSlot(), index, name == null ? "" : name));
    }

    private void refreshButtonStates() {
        List<RemoteDialerItem.Entry> entries = menu.getEntries();
        if (selectedIndex >= entries.size()) {
            selectedIndex = entries.isEmpty() ? -1 : entries.size() - 1;
        }

        boolean hasSelection = selectedIndex >= 0;

        this.selectButton.active = hasSelection && entries.get(selectedIndex).valid();
        this.deleteButton.active = hasSelection;
        this.renameButton.active = hasSelection && !this.renameInput.getValue().trim().isEmpty();
        this.revalidateButton.active = true;

        int maxOffset = Math.max(0, entries.size() - VISIBLE_ROWS);
        if (scrollOffset > maxOffset) {
            scrollOffset = maxOffset;
        }

        this.scrollUpButton.active = scrollOffset > 0;
        this.scrollDownButton.active = scrollOffset < maxOffset;
    }
}
