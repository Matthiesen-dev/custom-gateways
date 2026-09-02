package dev.matthiesen.custom_gateways.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface DimensionalLink {
    void setLinkedTarget(ResourceLocation dimension, BlockPos targetPos, boolean triggerLinkAnimation);
    void clearLinkedTarget();

    boolean isLinked();
}
