package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.common.matthiesen_lib.registry.AbstractBlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class BlockEntityRegistry extends AbstractBlockEntityRegistry {
    private static final BlockEntityRegistry INSTANCE = new BlockEntityRegistry();

    protected BlockEntityRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    private static Block[] resolveBlocks(Map<String, ? extends Supplier<? extends Block>> registeredBlocks) {
        return registeredBlocks.values()
                .stream()
                .map(Supplier::get)
                .toArray(Block[]::new);
    }

    private static <T extends BlockEntity> BlockEntityType<T> buildType(
            BiFunction<BlockPos, BlockState, T> entityFactory,
            Map<String, ? extends Supplier<? extends Block>> registeredBlocks
    ) {
        return BlockEntityType.Builder.of(entityFactory::apply, resolveBlocks(registeredBlocks)).build(null);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, Supplier<BlockEntityType<T>> blockEntity) {
        return INSTANCE.register(id, blockEntity);
    }
}
