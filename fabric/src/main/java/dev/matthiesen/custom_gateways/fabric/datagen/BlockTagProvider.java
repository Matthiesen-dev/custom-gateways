package dev.matthiesen.custom_gateways.fabric.datagen;

import dev.matthiesen.custom_gateways.common.datagen.TagsGeneration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public final class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        TagsGeneration.TAG_BLOCKS.forEach((tag, block) ->
                getOrCreateTagBuilder(tag).add(block));
        TagsGeneration.TAG_BLOCK_LISTS.forEach((tag, blockList) ->
                getOrCreateTagBuilder(tag).addTag(blockList));
    }
}
