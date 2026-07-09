package dev.matthiesen.custom_gateways.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Handles dimension-aware teleportation logic
 */
public final class PortalTeleporter {

    /**
     * Teleports an entity to a target position in a potentially different dimension
     */
    public static void teleportEntity(Entity entity, ServerLevel targetLevel, BlockPos targetPos) {
        if (!(entity instanceof Player player)) {
            teleportNonPlayer(entity, targetLevel, targetPos);
            return;
        }

        // Find a safe landing spot
        BlockPos safeLandingSpot = PortalValidation.findSafeLandingSpot(targetLevel, targetPos);
        Vec3 targetVec = Vec3.atCenterOf(safeLandingSpot);

        ServerLevel currentLevel = (ServerLevel) entity.level();
        if (currentLevel == targetLevel) {
            // Same dimension - simple teleport
            player.teleportTo(targetVec.x, targetVec.y, targetVec.z);
        } else {
            // Different dimension - use changeDimension with a proper transition
            player.changeDimension(new DimensionTransition(
                targetLevel,
                targetVec,
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                e -> {} // no-op post-transition
            ));
        }

        // Apply cooldown
        PlayerCooldownTracker.setCooldown(player);

        // Send feedback message
        player.displayClientMessage(
            Component.literal("§6Teleported to §e" + safeLocationString(targetLevel.dimension().location(), safeLandingSpot)),
            false
        );
    }

    /**
     * Teleports a non-player entity to a target position
     */
    private static void teleportNonPlayer(Entity entity, ServerLevel targetLevel, BlockPos targetPos) {
        BlockPos safeLandingSpot = PortalValidation.findSafeLandingSpot(targetLevel, targetPos);

        ServerLevel currentLevel = (ServerLevel) entity.level();
        if (currentLevel == targetLevel) {
            entity.teleportTo(safeLandingSpot.getX() + 0.5, safeLandingSpot.getY(), safeLandingSpot.getZ() + 0.5);
        } else {
            entity.discard();
            Entity newEntity = entity.getType().create(targetLevel);
            if (newEntity != null) {
                newEntity.moveTo(
                    safeLandingSpot.getX() + 0.5,
                    safeLandingSpot.getY(),
                    safeLandingSpot.getZ() + 0.5,
                    entity.getYRot(),
                    entity.getXRot()
                );
                targetLevel.addFreshEntity(newEntity);
            }
        }
    }

    /**
     * Helper to format location string for chat messages
     */
    private static String safeLocationString(ResourceLocation dimension, BlockPos pos) {
        return dimension.getPath() + " [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]";
    }
}
