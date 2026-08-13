package dev.matthiesen.custom_gateways.common.util;

import net.minecraft.resources.ResourceLocation;

public enum DimensionVariants {
    OVERWORLD("minecraft:overworld"),
    NETHER("minecraft:the_nether"),
    END("minecraft:the_end");

    private final ResourceLocation dimension;

    DimensionVariants(String dimension) {
        this.dimension = ResourceLocation.parse(dimension);
    }

    public String getDimension() {
        if (dimension.getNamespace().equalsIgnoreCase("minecraft")) {
            return dimension.getPath().toLowerCase();
        }
        return dimension.getNamespace() + "_" + dimension.getPath().toLowerCase();
    }

    public static DimensionVariants fromResourceLocation(ResourceLocation resourceLocation) {
        String dimension = resourceLocation.toString();
        for (DimensionVariants variant : values()) {
            if (variant.dimension.toString().equalsIgnoreCase(dimension)) {
                return variant;
            }
        }
        return null;
    }
}
