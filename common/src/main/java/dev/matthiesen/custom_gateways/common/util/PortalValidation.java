package dev.matthiesen.custom_gateways.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Validates and finds safe teleportation destinations
 */
public final class PortalValidation {

    /**
     * Checks if a block position is safe for the player to stand on
     */
    public static boolean isSafeLandingSpot(Level level, BlockPos pos) {
        // Check if there's a solid block below to stand on
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        boolean hasSupportBelow = belowState.isFaceSturdy(level, belowPos, Direction.UP)
                || belowState.getBlock() == Blocks.SCAFFOLDING;

        if (!hasSupportBelow) {
            return false;
        }

        // Check if the two blocks where the player will be/are not solid (must be passable)
        BlockState headState = level.getBlockState(pos);
        BlockState aboveHeadState = level.getBlockState(pos.above());

        // These blocks must not be solid so the player can occupy them
        if (!headState.getCollisionShape(level, pos).isEmpty()) {
            return false;
        }

        return aboveHeadState.getCollisionShape(level, pos.above()).isEmpty();
    }

    /**
     * Finds a safe landing spot near the target position
     * Searches in a small radius and returns the first safe spot found
     */
    public static BlockPos findSafeLandingSpot(Level level, BlockPos targetPos) {
        // First check if the target position is safe
        if (isSafeLandingSpot(level, targetPos)) {
            return targetPos;
        }

        // Search in expanding rings around the target
        int searchRadius = 5;
        for (int radius = 1; radius <= searchRadius; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    // Only check perimeter of current radius
                    if (Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }

                    BlockPos checkPos = targetPos.offset(x, 0, z);
                    if (isSafeLandingSpot(level, checkPos)) {
                        return checkPos;
                    }
                }
            }
        }

        // If no safe spot found, return the target anyway (player might take damage but won't be stuck)
        return targetPos;
    }
}




