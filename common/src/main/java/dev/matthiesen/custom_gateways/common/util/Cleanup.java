package dev.matthiesen.custom_gateways.common.util;

import dev.matthiesen.custom_gateways.common.block.entity.AncientPortalEntity;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.block.entity.PortalPadEntity;
import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class Cleanup {
    private Cleanup() {
        // Private constructor to prevent instantiation
    }

    private static @Nullable ServerLevel resolveLevel(ServerLevel currentLevel, ResourceLocation dimension) {
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimension);
        return currentLevel.getServer().getLevel(dimensionKey);
    }

    public static void portalLinks(ServerLevel level, BlockPos blockPos) {
        PortalRegistry registry = PortalRegistry.get(level);
        PortalRegistry.PortalLocation portalLocation =
                new PortalRegistry.PortalLocation(level.dimension().location(), blockPos);

        PortalRegistry.PortalLocation linkedLocation = registry.getLinkedPortal(portalLocation);
        registry.removePortal(portalLocation);

        if (linkedLocation != null) {
            ServerLevel linkedLevel = resolveLevel(level, linkedLocation.dimension());
            if (linkedLevel == null) {
                return;
            }

            BlockEntity linkedEntity = linkedLevel.getBlockEntity(linkedLocation.getBlockPos());
            if (linkedEntity instanceof AncientPortalEntity linkedAncientPortalEntity) {
                linkedAncientPortalEntity.clearLinkedTarget();
            } else if (linkedEntity instanceof PortalFrameEntity linkedPortalEntity) {
                linkedPortalEntity.clearLinkedTarget();
            } else if (linkedEntity instanceof PortalPadEntity linkedPortalPadEntity) {
                linkedPortalPadEntity.setLinked(false);
            }
        }

    }
}
