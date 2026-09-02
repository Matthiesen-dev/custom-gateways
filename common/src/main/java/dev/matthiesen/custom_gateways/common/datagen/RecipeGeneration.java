package dev.matthiesen.custom_gateways.common.datagen;

import dev.matthiesen.custom_gateways.common.registry.ItemRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RecipeGeneration {
    public static List<RecipeBuilder> RECIPES = new ArrayList<>();

    public static RecipeBuilder ANCIENT_PORTAL;
    public static RecipeBuilder NETHER_GATE;
    public static RecipeBuilder PORTAL_FRAME;
    public static RecipeBuilder PORTAL_LINKING_DEVICE;
    public static RecipeBuilder PORTAL_PAD;
    public static RecipeBuilder PORTAL_STONE;
    public static RecipeBuilder REMOTE_DIALER;

    static {
        ANCIENT_PORTAL = registerCraftingRecipe(
                ShapedRecipeBuilder.shaped(
                                RecipeCategory.MISC,
                                ItemRegistry.ANCIENT_PORTAL.get()
                        )
                        .group("custom_gateways")
                        .define('p', Items.COBBLED_DEEPSLATE)
                        .define('e', Items.ENDER_EYE)
                        .define('i', ItemRegistry.PORTAL_PAD.get())
                        .define('r', Items.REDSTONE_LAMP)
                        .pattern("r r")
                        .pattern("pep")
                        .pattern("pip")
                        .showNotification(true)
                        .unlockedBy("has_cobbled_deepslate", has(Items.COBBLED_DEEPSLATE))
                        .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                        .unlockedBy("has_portal_pad", has(ItemRegistry.PORTAL_PAD.get()))
                        .unlockedBy("has_redstone_lamp", has(Items.REDSTONE_LAMP))
        );

        NETHER_GATE = registerCraftingRecipe(
                ShapedRecipeBuilder.shaped(
                        RecipeCategory.MISC,
                        ItemRegistry.NETHER_GATE.get()
                )
                        .group("custom_gateways")
                        .define('p', Items.NETHER_BRICK)
                        .define('i', ItemRegistry.PORTAL_PAD.get())
                        .pattern(" p ")
                        .pattern("pip")
                        .showNotification(true)
                        .unlockedBy("has_nether_brick", has(Items.NETHER_BRICK))
                        .unlockedBy("has_portal_pad", has(ItemRegistry.PORTAL_PAD.get()))
        );

        PORTAL_FRAME = registerCraftingRecipe(
                ShapedRecipeBuilder.shaped(
                        RecipeCategory.MISC,
                        ItemRegistry.PORTAL_FRAME.get()
                )
                        .group("custom_gateways")
                        .define('r', Items.REDSTONE_LAMP)
                        .define('e', Items.ENDER_EYE)
                        .define('p', Items.IRON_BARS)
                        .define('i', ItemRegistry.PORTAL_PAD.get())
                        .pattern("r r")
                        .pattern("epe")
                        .pattern("pip")
                        .showNotification(true)
                        .unlockedBy("has_redstone_lamp", has(Items.REDSTONE_LAMP))
                        .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                        .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                        .unlockedBy("has_portal_pad", has(ItemRegistry.PORTAL_PAD.get()))
        );

        PORTAL_LINKING_DEVICE = registerCraftingRecipe(
                ShapedRecipeBuilder.shaped(
                        RecipeCategory.MISC,
                        ItemRegistry.PORTAL_LINKING_CARD.get()
                )
                        .group("custom_gateways")
                        .define('r', Items.REDSTONE)
                        .define('i', Items.IRON_NUGGET)
                        .define('e', Items.ENDER_EYE)
                        .define('p', Items.PAPER)
                        .pattern("rir")
                        .pattern("pep")
                        .pattern("rir")
                        .showNotification(true)
                        .unlockedBy("has_redstone", has(Items.REDSTONE))
                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                        .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                        .unlockedBy("has_paper", has(Items.PAPER))
        );

        PORTAL_PAD = registerCraftingRecipe(
                ShapedRecipeBuilder.shaped(
                        RecipeCategory.MISC,
                        ItemRegistry.PORTAL_PAD.get()
                )
                        .group("custom_gateways")
                        .define('e', Items.ENDER_PEARL)
                        .define('p', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                        .define('i', Items.CHORUS_FRUIT)
                        .pattern("epe")
                        .pattern("pip")
                        .showNotification(true)
                        .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                        .unlockedBy("has_heavy_weighted_pressure_plate", has(Items.HEAVY_WEIGHTED_PRESSURE_PLATE))
                        .unlockedBy("has_chorus_fruit", has(Items.CHORUS_FRUIT))
        );

        PORTAL_STONE = registerCraftingRecipe(
                ShapedRecipeBuilder.shaped(
                        RecipeCategory.MISC,
                        ItemRegistry.PORTAL_STONE.get()
                )
                        .group("custom_gateways")
                        .define('p', Items.COBBLED_DEEPSLATE)
                        .define('i', ItemRegistry.PORTAL_PAD.get())
                        .define('a', Items.AMETHYST_SHARD)
                        .pattern(" p ")
                        .pattern("pap")
                        .pattern("pip")
                        .showNotification(true)
                        .unlockedBy("has_cobbled_deepslate", has(Items.COBBLED_DEEPSLATE))
                        .unlockedBy("has_portal_pad", has(ItemRegistry.PORTAL_PAD.get()))
                        .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
        );

        REMOTE_DIALER = registerCraftingRecipe(
                ShapedRecipeBuilder.shaped(
                        RecipeCategory.MISC,
                        ItemRegistry.REMOTE_DIALER.get()
                )
                        .group("custom_gateways")
                        .define('p', Items.ENDER_EYE)
                        .define('e', ItemRegistry.PORTAL_LINKING_CARD.get())
                        .define('r', Items.CHORUS_FRUIT)
                        .define('i', Items.NETHERITE_INGOT)
                        .define('b', Items.BLAZE_ROD)
                        .pattern("rbr")
                        .pattern("pep")
                        .pattern("rir")
                        .showNotification(true)
                        .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                        .unlockedBy("has_portal_linking_card", has(ItemRegistry.PORTAL_LINKING_CARD.get()))
                        .unlockedBy("has_chorus_fruit", has(Items.CHORUS_FRUIT))
                        .unlockedBy("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                        .unlockedBy("has_blaze_rod", has(Items.BLAZE_ROD))
        );
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
                new InventoryChangeTrigger.TriggerInstance(
                        Optional.empty(),
                        InventoryChangeTrigger.TriggerInstance.Slots.ANY,
                        List.of(ItemPredicate.Builder.item().of(new ItemLike[]{item}).build())
                )
        );
    }

    private static RecipeBuilder registerCraftingRecipe(RecipeBuilder recipe) {
        RECIPES.add(recipe);
        return recipe;
    }
}
