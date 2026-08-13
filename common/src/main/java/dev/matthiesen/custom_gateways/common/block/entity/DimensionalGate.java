package dev.matthiesen.custom_gateways.common.block.entity;

import dev.matthiesen.custom_gateways.common.util.DimensionVariants;
import net.minecraft.resources.ResourceLocation;

public interface DimensionalGate {
    ResourceLocation getLinkedDimension();

    default DimensionVariants getDimensionVariant() {
        return DimensionVariants.fromResourceLocation(getLinkedDimension());
    }
}
