package dev.matthiesen.custom_gateways.common;

import dev.matthiesen.custom_gateways.common.block.*;
import dev.matthiesen.custom_gateways.common.block.entity.*;
import dev.matthiesen.custom_gateways.common.client.geckolib.*;
import dev.matthiesen.custom_gateways.common.client.screen.RemoteDialerScreen;
import dev.matthiesen.custom_gateways.common.item.*;
import dev.matthiesen.custom_gateways.common.network.GatewayEffectHandler;
import dev.matthiesen.custom_gateways.common.registry.*;
import dev.matthiesen.matthiesen_core.common.AbstractCommonClientMod;
import dev.matthiesen.matthiesen_core.common.api.events.PlatformClientEvents;
import dev.matthiesen.matthiesen_core.common.api.events.client.ClientEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

        INSTANCE.getScreenManager().registerMenuScreen(MenuRegistry.REMOTE_DIALER_MENU, RemoteDialerScreen::new);
        PlatformClientEvents.BLOCK_HIGHLIGHT.subscribe(this::onBlockHighlight);
        PlatformClientEvents.CLIENT_END_TICK.subscribe(this::onClientEndTick);
        PlatformClientEvents.CLIENT_STOPPING.subscribe(this::onClientStopping);
    }

    public InteractionResult onBlockHighlight(ClientEvent.BlockHighlight event) {
        var context = event.context();
        ClientLevel level = context.level();
        BlockPos basePos = getBasePos(level, context.blockHitResult().getBlockPos());
        if (basePos == null) return InteractionResult.PASS;

        Camera camera = context.camera();
        double x = basePos.getX() - camera.getPosition().x();
        double y = basePos.getY() - camera.getPosition().y();
        double z = basePos.getZ() - camera.getPosition().z();

        LevelRenderer.renderVoxelShape(
                context.poseStack(),
                context.multiBufferSource().getBuffer(RenderType.lines()),
                level.getBlockState(basePos).getShape(level, basePos),
                x, y, z,
                0.0F, 0.0F, 0.0F, 0.4F, false
        );
        return InteractionResult.FAIL;
    }

    public void onClientEndTick(ClientEvent.EndTick event) {
        GatewayEffectHandler.tick(event.client());
    }

    public void onClientStopping(ClientEvent.Stopping event) {
        GatewayEffectHandler.clear();
    }

    public void registerRenderers() {
        CustomGatewaysCommonClient.INSTANCE.createInfoLog("Registering Renderers");

        ItemRegistry.ANCIENT_PORTAL.get().renderProviderHolder.setValue(makeRendererProvider(AncientPortalRenderer.INSTANCE.getItemRenderer()));
        ItemRegistry.PORTAL_FRAME.get().renderProviderHolder.setValue(makeRendererProvider(PortalFrameRenderer.INSTANCE.getItemRenderer()));
        ItemRegistry.PORTAL_PAD.get().renderProviderHolder.setValue(makeRendererProvider(PortalPadRenderer.INSTANCE.getItemRenderer()));
        ItemRegistry.PORTAL_STONE.get().renderProviderHolder.setValue(makeRendererProvider(PortalStoneRenderer.INSTANCE.getItemRenderer()));
        ItemRegistry.NETHER_GATE.get().renderProviderHolder.setValue(makeRendererProvider(NetherGateRenderer.INSTANCE.getItemRenderer()));

        INSTANCE.getEntityRendererManager().registerEntityRenderers(registry ->
                {
                    registry.registerBlockEntityRenderer(BlockEntityRegistry.ANCIENT_PORTAL_BE.get(), context -> AncientPortalRenderer.INSTANCE.getBlockRenderer());
                    registry.registerBlockEntityRenderer(BlockEntityRegistry.PORTAL_FRAME_BE.get(), context -> PortalFrameRenderer.INSTANCE.getBlockRenderer());
                    registry.registerBlockEntityRenderer(BlockEntityRegistry.PORTAL_PAD_BE.get(), context -> PortalPadRenderer.INSTANCE.getBlockRenderer());
                    registry.registerBlockEntityRenderer(BlockEntityRegistry.PORTAL_STONE_BE.get(), context -> PortalStoneRenderer.INSTANCE.getBlockRenderer());
                    registry.registerBlockEntityRenderer(BlockEntityRegistry.REMOTE_GATEWAY_BE.get(), context -> RemoteGatewayBlockRenderer.INSTANCE.getRenderer());
                    registry.registerBlockEntityRenderer(BlockEntityRegistry.NETHER_GATE_BE.get(), context -> NetherGateRenderer.INSTANCE.getBlockRenderer());
                }
        );
    }

    public static @Nullable BlockPos getBasePos(Level level, BlockPos hitPos) {
        BlockState hitState = level.getBlockState(hitPos);

        if (hitState.is(BlockRegistry.ANCIENT_PORTAL.get()) && hitState.getBlock() instanceof AncientPortalBlock ancientPortalBlock) {
            return ancientPortalBlock.getMasterPos(level, hitPos);
        }

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

        if (hitState.is(BlockRegistry.NETHER_GATE.get())) {
            return hitPos;
        }

        if (hitState.is(BlockRegistry.PORTAL_STONE.get()) && hitState.getBlock() instanceof PortalStoneBlock portalStoneBlock) {
            return portalStoneBlock.getMasterPos(level, hitPos);
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
