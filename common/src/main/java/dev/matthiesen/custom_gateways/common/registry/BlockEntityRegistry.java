package dev.matthiesen.custom_gateways.common.registry;

import dev.matthiesen.matthiesen_core.common.registry.AbstractBlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.block.entity.PortalPadEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class BlockEntityRegistry extends AbstractBlockEntityRegistry {
    private static final BlockEntityRegistry INSTANCE = new BlockEntityRegistry();

    private BlockEntityRegistry() {
        super(CustomGatewaysCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<BlockEntityType<PortalFrameEntity>> PORTAL_FRAME_BE;
    public static final Supplier<BlockEntityType<PortalPadEntity>> PORTAL_PAD_BE;

    static {
        PORTAL_FRAME_BE = registerBlockEntity("portal_frame", () ->
                buildType(PortalFrameEntity::new, BlockRegistry.PORTAL_FRAME));
        PORTAL_PAD_BE = registerBlockEntity("portal_pad", () ->
                buildType(PortalPadEntity::new, BlockRegistry.PORTAL_PAD));
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends BlockEntity> BlockEntityType<T> buildType(
            BiFunction<BlockPos, BlockState, T> entityFactory,
            Supplier<? extends Block> supplier
    ) {
        return BlockEntityType.Builder.of(entityFactory::apply, supplier.get()).build(null);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, Supplier<BlockEntityType<T>> blockEntity) {
        return INSTANCE.register(id, blockEntity);
    }
}
