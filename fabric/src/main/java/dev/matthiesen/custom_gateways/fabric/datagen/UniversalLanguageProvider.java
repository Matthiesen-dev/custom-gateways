package dev.matthiesen.custom_gateways.fabric.datagen;

import dev.matthiesen.custom_gateways.common.datagen.GlobalTranslations;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class UniversalLanguageProvider extends FabricLanguageProvider {
    private final String languageCode;

    public UniversalLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup, String languageCode) {
        super(dataOutput, languageCode, registryLookup);
        this.languageCode = languageCode;
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        Map<String, String> localeTranslations = GlobalTranslations.getLocale(this.languageCode);

        if (localeTranslations == null) {
            throw new IllegalStateException("No translations registered for locale: " + this.languageCode);
        }

        for (var entry : localeTranslations.entrySet()) {
            translationBuilder.add(entry.getKey(), entry.getValue());
        }
    }
}

