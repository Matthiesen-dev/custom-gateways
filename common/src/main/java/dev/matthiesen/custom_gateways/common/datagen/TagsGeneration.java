package dev.matthiesen.custom_gateways.common.datagen;

import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import dev.matthiesen.custom_gateways.common.registry.ItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class TagsGeneration {

    public static class ItemTags {
        public static final TagKey<Item> CUSTOM_GATEWAYS_TOOLS = createTag("custom_gateways", "tools");

        @SuppressWarnings("SameParameterValue")
        private static TagKey<Item> createTag(String namespace, String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
        }
    }

    public static class BlockTags {
        public static final TagKey<Block> C_RELOCATION_NOT_SUPPORTED = createTag("c", "relocation_not_supported");
        public static final TagKey<Block> MINECRAFT_NEEDS_IRON_TOOL = createTag("minecraft", "needs_iron_tool");
        public static final TagKey<Block> MINECRAFT_MINEABLE_PICKAXE = createTag("minecraft", "mineable/pickaxe");
        public static final TagKey<Block> CUSTOM_GATEWAYS_ALL_PORTALS = createTag("custom_gateways", "all_portals");
        public static final TagKey<Block> CUSTOM_GATEWAYS_MINEABLE_PORTALS = createTag("custom_gateways", "mineable_portals");
        public static final TagKey<Block> CUSTOM_GATEWAYS_PORTAL_LINK_DESTINATIONS = createTag("custom_gateways", "portal_link_destinations");
        public static final TagKey<Block> CUSTOM_GATEWAYS_PORTAL_LINK_SOURCES = createTag("custom_gateways", "portal_link_sources");

        private static TagKey<Block> createTag(String namespace, String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path));
        }
    }

    public static Map<TagKey<Item>, Item[]> TAG_ITEMS = new HashMap<>();

    public static Map<TagKey<Block>, Block[]> TAG_BLOCKS = new HashMap<>();
    public static Map<TagKey<Block>, TagKey<Block>> TAG_BLOCK_LISTS = new HashMap<>();

    static {
        TAG_ITEMS.put(ItemTags.CUSTOM_GATEWAYS_TOOLS, ItemRegistry.ALL_TOOLS.stream().map(Supplier::get).toArray(Item[]::new));

        TAG_BLOCK_LISTS.put(BlockTags.C_RELOCATION_NOT_SUPPORTED, BlockTags.CUSTOM_GATEWAYS_ALL_PORTALS);
        TAG_BLOCK_LISTS.put(BlockTags.MINECRAFT_MINEABLE_PICKAXE, BlockTags.CUSTOM_GATEWAYS_MINEABLE_PORTALS);
        TAG_BLOCK_LISTS.put(BlockTags.MINECRAFT_NEEDS_IRON_TOOL, BlockTags.CUSTOM_GATEWAYS_MINEABLE_PORTALS);

        List<Block> PORTAL_SOURCES = new ArrayList<>();
        PORTAL_SOURCES.add(BlockRegistry.PORTAL_FRAME.get());
        PORTAL_SOURCES.add(BlockRegistry.ANCIENT_PORTAL.get());
        PORTAL_SOURCES.add(BlockRegistry.NETHER_GATE.get());

        List<Block> PORTAL_DESTINATIONS = new ArrayList<>(PORTAL_SOURCES);
        PORTAL_DESTINATIONS.add(BlockRegistry.PORTAL_PAD.get());
        PORTAL_DESTINATIONS.add(BlockRegistry.PORTAL_STONE.get());

        List<Block> ALL_PORTALS = new ArrayList<>(PORTAL_DESTINATIONS);
        ALL_PORTALS.add(BlockRegistry.REMOTE_GATEWAY.get());

        TAG_BLOCKS.put(BlockTags.CUSTOM_GATEWAYS_ALL_PORTALS, ALL_PORTALS.toArray(new Block[0]));
        TAG_BLOCKS.put(BlockTags.CUSTOM_GATEWAYS_MINEABLE_PORTALS, PORTAL_DESTINATIONS.toArray(new Block[0]));
        TAG_BLOCKS.put(BlockTags.CUSTOM_GATEWAYS_PORTAL_LINK_DESTINATIONS, PORTAL_DESTINATIONS.toArray(new Block[0]));
        TAG_BLOCKS.put(BlockTags.CUSTOM_GATEWAYS_PORTAL_LINK_SOURCES, PORTAL_SOURCES.toArray(new Block[0]));
    }
}
