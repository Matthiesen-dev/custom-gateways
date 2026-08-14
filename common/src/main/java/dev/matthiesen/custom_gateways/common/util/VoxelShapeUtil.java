package dev.matthiesen.custom_gateways.common.util;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Shared utilities for building and transforming {@link VoxelShape}s. */
public final class VoxelShapeUtil {
    private VoxelShapeUtil() {}

    /**
     * Rotates {@code base} to face {@code direction} (NORTH is identity).
     * Each 90-degree step maps (x, y, z) → (1−z, y, x).
     */
    public static VoxelShape calculateRotation(Direction direction, VoxelShape base) {
        VoxelShape[] buffer = new VoxelShape[]{base, Shapes.empty()};
        int times = (direction.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                buffer[1] = Shapes.joinUnoptimized(buffer[1],
                    Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX), BooleanOp.OR));
            buffer[0] = buffer[1].optimize();
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

    /** Returns a uniformly distributed random value in {@code [min, max)}. */
    public static double randomBetween(RandomSource random, double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}

