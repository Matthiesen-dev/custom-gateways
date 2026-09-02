package dev.matthiesen.custom_gateways.common.datagen.locale;

import dev.matthiesen.custom_gateways.common.datagen.GlobalTranslations;
import dev.matthiesen.custom_gateways.common.datagen.util.TranslationBuilder;

public final class EnUsTranslations {
    private static final TranslationBuilder TRANSLATIONS = new TranslationBuilder("en_us");

    public static void registerTranslations() {
        GlobalTranslations.addTranslations(TRANSLATIONS);
    }
}
