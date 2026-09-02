package dev.matthiesen.custom_gateways.common.datagen;

import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class LootTableGeneration {
    public static final List<Block> BLOCKS_SELF_DROP = new ArrayList<>();

    static {
        registerSelfDrop(BlockRegistry.ANCIENT_PORTAL.get());
        registerSelfDrop(BlockRegistry.NETHER_GATE.get());
        registerSelfDrop(BlockRegistry.PORTAL_FRAME.get());
        registerSelfDrop(BlockRegistry.PORTAL_PAD.get());
        registerSelfDrop(BlockRegistry.PORTAL_STONE.get());
    }

    public static void registerSelfDrop(Block block) {
        BLOCKS_SELF_DROP.add(block);
    }
}
