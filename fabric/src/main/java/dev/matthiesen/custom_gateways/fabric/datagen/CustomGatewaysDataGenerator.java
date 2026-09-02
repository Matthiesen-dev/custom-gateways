package dev.matthiesen.custom_gateways.fabric.datagen;

import dev.matthiesen.custom_gateways.common.datagen.GlobalTranslations;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class CustomGatewaysDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // Data Providers
        pack.addProvider(AdvancementProvider::new);
        pack.addProvider(BlockLootTableProvider::new);
        pack.addProvider(BlockTagProvider::new);
        pack.addProvider(ItemTagProvider::new);
        pack.addProvider(RecipeProvider::new);

        // Translations
        GlobalTranslations.init();
        for (var entry : GlobalTranslations.TRANSLATIONS.entrySet()) {
            var locale = entry.getKey();
            pack.addProvider((output, registryLookup) ->
                    new UniversalLanguageProvider(output, registryLookup, locale));
        }
    }
}
