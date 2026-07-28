package dev.matthiesen.custom_gateways.common.item;

import dev.matthiesen.custom_gateways.common.block.entity.RemoteGatewayBlockEntity;
import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import dev.matthiesen.custom_gateways.common.menu.RemoteDialerMenu;
import dev.matthiesen.custom_gateways.common.util.PortalLinkTags;
import dev.matthiesen.custom_gateways.common.util.PortalValidation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class RemoteDialerItem extends Item {
    public static final int MAX_ENTRIES = 32;

    private static final String DIALER_DATA_TAG = "dialer_data";
    private static final String DIALER_ENTRIES_TAG = "entries";
    private static final String ENTRY_NAME_TAG = "name";
    private static final String ENTRY_DIMENSION_TAG = "dimension";
    private static final String ENTRY_X_TAG = "x";
    private static final String ENTRY_Y_TAG = "y";
    private static final String ENTRY_Z_TAG = "z";
    private static final String ENTRY_VALID_TAG = "valid";

    public RemoteDialerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!clickedState.is(PortalLinkTags.PORTAL_LINK_DESTINATIONS)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        Entry incoming = Entry.fromPortalLocation(level.dimension().location(), clickedPos, true);
        List<Entry> entries = readEntries(stack);

        int existingIndex = findByLocation(entries, incoming.location());
        if (existingIndex >= 0) {
            player.displayClientMessage(Component.translatable("interaction.custom_gateways.remote_dialer.add.duplicate"), true);
            return InteractionResult.CONSUME;
        }

        if (entries.size() >= MAX_ENTRIES) {
            player.displayClientMessage(Component.translatable("interaction.custom_gateways.remote_dialer.add.full", MAX_ENTRIES), true);
            return InteractionResult.CONSUME;
        }

        entries.add(incoming);
        writeEntries(stack, entries);
        player.displayClientMessage(Component.translatable("interaction.custom_gateways.remote_dialer.add.success", incoming.name()), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        revalidateEntries(stack, (ServerLevel) level);
        RemoteDialerMenu.open(serverPlayer, player.getInventory().selected);
        return InteractionResultHolder.consume(stack);
    }

    public static void revalidateEntries(ItemStack stack, ServerLevel currentLevel) {
        List<Entry> entries = readEntries(stack);
        boolean changed = false;

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            boolean valid = isValidDestination(entry.location(), currentLevel);
            if (entry.valid() != valid) {
                entries.set(i, entry.withValid(valid));
                changed = true;
            }
        }

        if (changed) {
            writeEntries(stack, entries);
        }
    }

    public static List<Entry> getEntries(ItemStack stack) {
        return readEntries(stack);
    }

    public static void deleteEntry(ItemStack stack, int entryIndex) {
        List<Entry> entries = readEntries(stack);
        if (entryIndex < 0 || entryIndex >= entries.size()) {
            return;
        }

        entries.remove(entryIndex);
        writeEntries(stack, entries);
    }

    public static boolean renameEntry(ItemStack stack, int entryIndex, String newName) {
        List<Entry> entries = readEntries(stack);
        if (entryIndex < 0 || entryIndex >= entries.size()) {
            return false;
        }

        String normalized = normalizeName(newName);
        if (normalized.isEmpty()) {
            return false;
        }

        Entry current = entries.get(entryIndex);
        entries.set(entryIndex, current.withName(normalized));
        writeEntries(stack, entries);
        return true;
    }

    public static boolean openGateway(ServerPlayer player, ItemStack stack, int entryIndex) {
        List<Entry> entries = readEntries(stack);
        if (entryIndex < 0 || entryIndex >= entries.size()) {
            return false;
        }

        Entry entry = entries.get(entryIndex);
        if (!entry.valid()) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        if (!isValidDestination(entry.location(), level)) {
            entries.set(entryIndex, entry.withValid(false));
            writeEntries(stack, entries);
            return false;
        }

        BlockPos spawnPos = PortalValidation.findRemoteGatewaySpawnPos(level, player);
        if (spawnPos == null) {
            player.displayClientMessage(Component.translatable("interaction.custom_gateways.remote_dialer.spawn.failed"), true);
            return false;
        }

        boolean spawned = RemoteGatewayBlockEntity.spawnGateway(level, spawnPos, entry.location(), player.getUUID());
        if (!spawned) {
            player.displayClientMessage(Component.translatable("interaction.custom_gateways.remote_dialer.spawn.failed"), true);
            return false;
        }

        player.displayClientMessage(Component.translatable("interaction.custom_gateways.remote_dialer.spawn.success", entry.name()), true);
        return true;
    }

    public static boolean isValidDestination(PortalRegistry.PortalLocation location, ServerLevel currentLevel) {
        ServerLevel destinationLevel = resolveLevel(currentLevel, location.dimension());
        if (destinationLevel == null) {
            return false;
        }

        BlockState state = destinationLevel.getBlockState(location.getBlockPos());
        return state.is(PortalLinkTags.PORTAL_LINK_DESTINATIONS);
    }

    private static @Nullable ServerLevel resolveLevel(ServerLevel currentLevel, ResourceLocation dimension) {
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimension);
        return currentLevel.getServer().getLevel(key);
    }

    private static List<Entry> readEntries(ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag data = root.getCompound(DIALER_DATA_TAG);
        ListTag listTag = data.getList(DIALER_ENTRIES_TAG, Tag.TAG_COMPOUND);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < listTag.size() && entries.size() < MAX_ENTRIES; i++) {
            CompoundTag entryTag = listTag.getCompound(i);
            String dimensionRaw = entryTag.getString(ENTRY_DIMENSION_TAG);
            if (dimensionRaw.isEmpty()) {
                continue;
            }

            ResourceLocation dimension;
            try {
                dimension = ResourceLocation.parse(dimensionRaw);
            } catch (IllegalArgumentException ex) {
                continue;
            }

            int x = entryTag.getInt(ENTRY_X_TAG);
            int y = entryTag.getInt(ENTRY_Y_TAG);
            int z = entryTag.getInt(ENTRY_Z_TAG);
            String name = normalizeName(entryTag.getString(ENTRY_NAME_TAG));
            boolean valid = entryTag.contains(ENTRY_VALID_TAG) && entryTag.getBoolean(ENTRY_VALID_TAG);

            PortalRegistry.PortalLocation location = new PortalRegistry.PortalLocation(dimension, x, y, z);
            if (name.isEmpty()) {
                name = defaultName(location);
            }
            entries.add(new Entry(name, location, valid));
        }

        return entries;
    }

    private static void writeEntries(ItemStack stack, List<Entry> entries) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag data = new CompoundTag();
        ListTag list = new ListTag();

        for (Entry entry : entries) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(ENTRY_NAME_TAG, normalizeName(entry.name()));
            entryTag.putString(ENTRY_DIMENSION_TAG, entry.location().dimension().toString());
            entryTag.putInt(ENTRY_X_TAG, entry.location().x());
            entryTag.putInt(ENTRY_Y_TAG, entry.location().y());
            entryTag.putInt(ENTRY_Z_TAG, entry.location().z());
            entryTag.putBoolean(ENTRY_VALID_TAG, entry.valid());
            list.add(entryTag);
        }

        data.put(DIALER_ENTRIES_TAG, list);
        root.put(DIALER_DATA_TAG, data);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static int findByLocation(List<Entry> entries, PortalRegistry.PortalLocation target) {
        for (int i = 0; i < entries.size(); i++) {
            if (Objects.equals(entries.get(i).location(), target)) {
                return i;
            }
        }
        return -1;
    }

    private static String normalizeName(String candidate) {
        if (candidate == null) {
            return "";
        }
        String trimmed = candidate.trim();
        if (trimmed.length() > 48) {
            return trimmed.substring(0, 48);
        }
        return trimmed;
    }

    private static String defaultName(PortalRegistry.PortalLocation location) {
        return "%s @ %d, %d, %d".formatted(
            location.dimension().toString().toLowerCase(Locale.ROOT),
            location.x(),
            location.y(),
            location.z()
        );
    }

    public record Entry(String name, PortalRegistry.PortalLocation location, boolean valid) {
        public static Entry fromPortalLocation(ResourceLocation dimension, BlockPos pos, boolean valid) {
            PortalRegistry.PortalLocation location = new PortalRegistry.PortalLocation(dimension, pos);
            return new Entry(defaultName(location), location, valid);
        }

        public Entry withValid(boolean nextValid) {
            return new Entry(this.name, this.location, nextValid);
        }

        public Entry withName(String nextName) {
            return new Entry(nextName, this.location, this.valid);
        }
    }
}
