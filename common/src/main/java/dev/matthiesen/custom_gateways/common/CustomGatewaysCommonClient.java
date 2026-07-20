package dev.matthiesen.custom_gateways.common;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.matthiesen.common.matthiesen_lib.abstracts.AbstractCommonClientMod;
import dev.matthiesen.custom_gateways.common.block.PortalFrameBlock;
import dev.matthiesen.custom_gateways.common.client.geckolib.PortalFrameBlockRenderer;
import dev.matthiesen.custom_gateways.common.client.geckolib.PortalPadBlockRenderer;
import dev.matthiesen.custom_gateways.common.client.geckolib.PortalFrameItemRenderer;
import dev.matthiesen.custom_gateways.common.client.geckolib.PortalPadItemRenderer;
import dev.matthiesen.custom_gateways.common.registry.BlockEntityRegistry;
import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import dev.matthiesen.custom_gateways.common.registry.ItemRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class CustomGatewaysCommonClient extends AbstractCommonClientMod {
    public static final CustomGatewaysCommonClient INSTANCE = new CustomGatewaysCommonClient();

    private CustomGatewaysCommonClient() {
        super(CustomGatewaysCommon.INSTANCE);
    }

    @Override
    public void initialize() {
        CustomGatewaysCommonClient.INSTANCE.createInfoLog("Initialized Common Client");
    }

    public void registerRenderers() {
        CustomGatewaysCommonClient.INSTANCE.createInfoLog("Registering Renderers");

        ItemRegistry.PORTAL_FRAME.get().renderProviderHolder.setValue(makeRendererProvider(new PortalFrameItemRenderer().getRenderer()));
        ItemRegistry.PORTAL_PAD.get().renderProviderHolder.setValue(makeRendererProvider(new PortalPadItemRenderer().getRenderer()));

        INSTANCE.registerEntityRenderers(registry ->
                {
                    registry.registerBlockEntityRenderer(BlockEntityRegistry.PORTAL_FRAME_BE.get(), context -> new PortalFrameBlockRenderer().getRenderer());
                    registry.registerBlockEntityRenderer(BlockEntityRegistry.PORTAL_PAD_BE.get(), context -> new PortalPadBlockRenderer().getRenderer());
                }
        );

        INSTANCE.registerBlockOutlineListener(context -> {
            ClientLevel level = context.level();
            BlockPos basePos = getBasePos(level, context.blockHitResult().getBlockPos());

            if (basePos == null) return true;

            PoseStack poseStack = context.poseStack();
            MultiBufferSource bufferSource = context.multiBufferSource();
            Camera camera = context.camera();
            VoxelShape shape = level.getBlockState(basePos).getShape(level, basePos);

            double x = basePos.getX() - camera.getPosition().x();
            double y = basePos.getY() - camera.getPosition().y();
            double z = basePos.getZ() - camera.getPosition().z();

            LevelRenderer.renderVoxelShape(
                    poseStack,
                    bufferSource.getBuffer(RenderType.lines()),
                    shape,
                    x, y, z,
                    0.0F, 0.0F, 0.0F, 0.4F, false
            );

            return false;
        });
    }

    public static @Nullable BlockPos getBasePos(Level level, BlockPos hitPos) {
        BlockState hitState = level.getBlockState(hitPos);

        if (hitState.is(BlockRegistry.PORTAL_FRAME.get())) {
            if (hitState.getValue(PortalFrameBlock.IS_SLAVE)) {
                return hitPos.below();
            } else {
                return hitPos;
            }
        }

        if (hitState.is(BlockRegistry.PORTAL_PAD.get())) {
            return hitPos;
        }

        return null;
    }

    private static <T extends Item & GeoItem> GeoRenderProvider makeRendererProvider(GeoItemRenderer<T> renderer) {
        return new GeoRenderProvider() {
            private BlockEntityWithoutLevelRenderer itemRenderer;

            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = renderer;
                }
                return this.itemRenderer;
            }
        };
    }
}
