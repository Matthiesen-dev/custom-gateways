package dev.matthiesen.custom_gateways.common.util;

import dev.matthiesen.custom_gateways.common.registry.CriterionRegistry;
import dev.matthiesen.custom_gateways.common.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
        if (safeLandingSpot == null) {
            // No safe landing spot found — play failure sound at the destination portal and abort.
            // TODO: also trigger this for portal blocked/broken state (future feature)
            targetLevel.playSound(null, targetPos, SoundRegistry.GATEWAY_TELEPORT_FAILURE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            return;
        }
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

        // Play teleport success sound at the destination
        targetLevel.playSound(null, safeLandingSpot, SoundRegistry.GATEWAY_TELEPORT_SUCCESS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        CriterionRegistry.USE_PORTAL.get().trigger((ServerPlayer) player);

        // Send feedback message
        player.displayClientMessage(
            Component.translatable("interaction.custom_gateways.portal_frame.telported", safeLocationString(targetLevel.dimension().location(), safeLandingSpot)),
            true
        );
    }

    /**
     * Teleports a non-player entity to a target position
     */
    private static void teleportNonPlayer(Entity entity, ServerLevel targetLevel, BlockPos targetPos) {
        BlockPos safeLandingSpot = PortalValidation.findSafeLandingSpot(targetLevel, targetPos);
        // Fall back to the raw target position if no safe spot is found
        if (safeLandingSpot == null) {
            safeLandingSpot = targetPos;
        }

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
