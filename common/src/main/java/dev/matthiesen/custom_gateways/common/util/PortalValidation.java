package dev.matthiesen.custom_gateways.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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
     * Finds a safe landing spot near the target position.
     * Searches in a small radius and returns the first safe spot found,
     * or {@code null} if no safe spot exists within the search radius.
     */
    public static @Nullable BlockPos findSafeLandingSpot(Level level, BlockPos targetPos) {
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

        // If no safe spot found, return null — caller must handle this case
        return null;
    }

    /**
     * Finds a safe 2-block-tall location for placing a temporary remote gateway.
     */
    public static @Nullable BlockPos findRemoteGatewaySpawnPos(Level level, ServerPlayer player) {
        Direction facing = player.getDirection();
        BlockPos playerPos = player.blockPosition();

        for (int distance = 3; distance <= 4; distance++) {
            BlockPos forward = playerPos.relative(facing, distance);
            BlockPos found = searchAroundForward(level, forward, playerPos.getY());
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private static @Nullable BlockPos searchAroundForward(Level level, BlockPos forward, int preferredY) {
        for (int radius = 0; radius <= 2; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }

                    BlockPos baseColumn = forward.offset(dx, 0, dz);
                    for (int dy = 3; dy >= -5; dy--) {
                        BlockPos basePos = new BlockPos(baseColumn.getX(), preferredY + dy, baseColumn.getZ());
                        if (isValidGatewayBase(level, basePos)) {
                            return basePos;
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean isValidGatewayBase(Level level, BlockPos basePos) {
        BlockPos belowPos = basePos.below();
        BlockState below = level.getBlockState(belowPos);
        if (!below.isFaceSturdy(level, belowPos, Direction.UP)) {
            return false;
        }

        BlockState base = level.getBlockState(basePos);
        BlockState top = level.getBlockState(basePos.above());

        if (!base.canBeReplaced() || !top.canBeReplaced()) {
            return false;
        }

        if (!base.getCollisionShape(level, basePos).isEmpty()) {
            return false;
        }

        return top.getCollisionShape(level, basePos.above()).isEmpty();
    }
}
