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
    private static final int VISIBLE_ROWS = 5;

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
        this.imageWidth = 256;
        this.imageHeight = 228;
    }

    @Override
    protected void init() {
        super.init();

        int left = this.leftPos;
        int top = this.topPos;

        // Title is at top
        // List area: 8px margin, 115px tall (5 rows x 18px + gaps)

        // Rename input and label
        this.renameInput = new EditBox(this.font, left + 8, top + 141, 240, 18, Component.translatable("menu.custom_gateways.remote_dialer.rename_input"));
        this.renameInput.setMaxLength(48);
        this.addRenderableWidget(this.renameInput);

        // Action buttons - organized in two columns
        this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("menu.custom_gateways.remote_dialer.open"),
            button -> {
                sendAction(RemoteDialerActionPayload.ACTION_SELECT);
                this.onClose();
            })
            .bounds(left + 8, top + 164, 75, 20)
            .build());

        this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("menu.custom_gateways.remote_dialer.delete"),
            button -> {
                int index = this.selectedIndex;
                sendAction(RemoteDialerActionPayload.ACTION_DELETE, "", index);
                applyClientDelete(index);
            })
            .bounds(left + 92, top + 164, 75, 20)
            .build());

        this.renameButton = this.addRenderableWidget(Button.builder(Component.translatable("menu.custom_gateways.remote_dialer.rename"),
            button -> {
                int index = this.selectedIndex;
                applyClientRename(index);
                sendAction(RemoteDialerActionPayload.ACTION_RENAME, this.renameInput.getValue(), index);
            })
            .bounds(left + 176, top + 164, 75, 20)
            .build());

        this.revalidateButton = this.addRenderableWidget(Button.builder(Component.translatable("menu.custom_gateways.remote_dialer.revalidate"),
            button -> sendAction(RemoteDialerActionPayload.ACTION_REVALIDATE))
            .bounds(left + 8, top + 188, 243, 20)
            .build());

        // Scroll buttons on the right side
        this.scrollUpButton = this.addRenderableWidget(Button.builder(Component.literal("▲"),
            button -> {
                if (scrollOffset > 0) {
                    scrollOffset--;
                }
            }).bounds(left + 240, top + 20, 16, 14).build());

        this.scrollDownButton = this.addRenderableWidget(Button.builder(Component.literal("▼"),
            button -> {
                int maxOffset = Math.max(0, menu.getEntries().size() - VISIBLE_ROWS);
                if (scrollOffset < maxOffset) {
                    scrollOffset++;
                }
            }).bounds(left + 240, top + 104, 16, 14).build());

        refreshButtonStates();
        syncRenameInputFromSelection();
    }

    @Override
    public void containerTick() {
        super.containerTick();

        // Only update rename input when selection changes, not every tick
        // This allows users to type freely without the field being reset
        refreshButtonStates();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // While editing a name, consume the inventory key so typing 'E' does not close the menu.
        if (this.renameInput.isFocused() && this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listLeft = this.leftPos + 8;
        int listTop = this.topPos + 20;
        int listWidth = 228;
        int listRight = listLeft + listWidth;
        int listBottom = listTop + (VISIBLE_ROWS * ROW_HEIGHT);

        // Check if click is in list area FIRST (before super processes it)
        if (mouseX >= listLeft && mouseX <= listRight && mouseY >= listTop && mouseY <= listBottom) {
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int rowY = listTop + row * ROW_HEIGHT;
                int rowYBottom = rowY + ROW_HEIGHT - 1;
                if (mouseY >= rowY && mouseY <= rowYBottom) {
                    int idx = scrollOffset + row;
                    if (idx < menu.getEntries().size()) {
                        this.selectedIndex = idx;
                        // Update rename input when selecting a new entry
                        this.renameInput.setValue(menu.getEntries().get(idx).name());
                        this.renameInput.setFocused(false);
                        refreshButtonStates();
                        return true;
                    }
                }
            }
        }

        // Now let super handle buttons and other widgets
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        // Main background — lighter panel
        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xE0262626);

        // Subtle title-bar stripe
        graphics.fill(left, top, left + this.imageWidth, top + 18, 0xE02e2e2e);

        // List area background
        graphics.fill(left + 8, top + 20, left + 236, top + 120, 0xFF111111);

        // List area border (1 px outline)
        int borderColor = 0xFF454545;
        graphics.fill(left + 7,   top + 19,  left + 237, top + 20,  borderColor); // top
        graphics.fill(left + 7,   top + 120, left + 237, top + 121, borderColor); // bottom
        graphics.fill(left + 7,   top + 19,  left + 8,   top + 121, borderColor); // left
        graphics.fill(left + 236, top + 19,  left + 237, top + 121, borderColor); // right

        // List entry rows
        List<RemoteDialerItem.Entry> entries = menu.getEntries();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int idx = scrollOffset + row;
            if (idx >= entries.size()) {
                break;
            }

            int y = top + 20 + row * ROW_HEIGHT;
            boolean selected = idx == selectedIndex;
            int rowColor = selected ? 0xFF1a4d1a : (row % 2 == 0 ? 0xFF181818 : 0xFF131313);
            graphics.fill(left + 8, y, left + 236, y + ROW_HEIGHT - 1, rowColor);
        }

        // Thin separator between list section and controls
        graphics.fill(left + 7, top + 126, left + 249, top + 127, 0xFF383838);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Title
        graphics.drawString(this.font, this.title, 8, 6, 0xFFFFFF, false);

        List<RemoteDialerItem.Entry> entries = menu.getEntries();

        if (entries.isEmpty()) {
            graphics.drawString(this.font, "No destinations added yet", 14, 60, 0x999999, false);
        } else {
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int idx = scrollOffset + row;
                if (idx >= entries.size()) {
                    break;
                }

                RemoteDialerItem.Entry entry = entries.get(idx);
                int color = entry.valid() ? 0x00FF00 : 0xFF6666;
                String label = entry.name();
                if (!entry.valid()) {
                    label += " [Invalid]";
                }

                int y = 20 + row * ROW_HEIGHT;
                graphics.drawString(this.font, label, 14, y + 4, color, false);
            }
        }

        // Rename input label
        graphics.drawString(this.font, "Name:", 8, 130, 0xAAAAAA, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void sendAction(int action) {
        int index = action == RemoteDialerActionPayload.ACTION_REVALIDATE ? -1 : this.selectedIndex;
        NetworkRegistry.sendToServer(new RemoteDialerActionPayload(action, menu.getDialerSlot(), index, ""));
    }

    private void sendAction(int action, String name, int entryIndex) {
        int index = action == RemoteDialerActionPayload.ACTION_REVALIDATE ? -1 : entryIndex;
        NetworkRegistry.sendToServer(new RemoteDialerActionPayload(action, menu.getDialerSlot(), index, name == null ? "" : name));
    }

    private void applyClientRename(int entryIndex) {
        if (entryIndex < 0) {
            return;
        }

        RemoteDialerItem.renameEntry(menu.getDialerStack(), entryIndex, this.renameInput.getValue());
        refreshButtonStates();
        syncRenameInputFromSelection();
    }

    private void applyClientDelete(int entryIndex) {
        if (entryIndex < 0) {
            return;
        }

        RemoteDialerItem.deleteEntry(menu.getDialerStack(), entryIndex);
        this.selectedIndex = entryIndex;
        refreshButtonStates();
        syncRenameInputFromSelection();
    }

    private void refreshButtonStates() {
        List<RemoteDialerItem.Entry> entries = menu.getEntries();

        // Auto-select first entry if there are entries but nothing is selected
        if (entries.isEmpty()) {
            selectedIndex = -1;
        } else if (selectedIndex < 0 || selectedIndex >= entries.size()) {
            selectedIndex = 0;
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

    private void syncRenameInputFromSelection() {
        List<RemoteDialerItem.Entry> entries = menu.getEntries();
        if (selectedIndex >= 0 && selectedIndex < entries.size()) {
            this.renameInput.setValue(entries.get(selectedIndex).name());
        } else {
            this.renameInput.setValue("");
        }
    }
}
