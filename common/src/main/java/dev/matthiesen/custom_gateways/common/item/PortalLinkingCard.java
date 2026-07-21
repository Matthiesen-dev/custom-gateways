package dev.matthiesen.custom_gateways.common.item;

import dev.matthiesen.custom_gateways.common.block.entity.PortalFrameEntity;
import dev.matthiesen.custom_gateways.common.block.entity.PortalPadEntity;
import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import dev.matthiesen.custom_gateways.common.registry.CriterionRegistry;
import dev.matthiesen.custom_gateways.common.registry.SoundRegistry;
import dev.matthiesen.custom_gateways.common.util.PortalLinkTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PortalLinkingCard extends Item {
    public static final String PORTAL_DATA_TAG = "portal_data";
    public static final String DIMENSION_TAG = "dimension";
    public static final String X_TAG = "x";
    public static final String Y_TAG = "y";
    public static final String Z_TAG = "z";
    private static final long DOUBLE_CROUCH_WINDOW_TICKS = 60L; // 3 seconds at 20 TPS
    private static final Map<UUID, Long> LAST_CROUCH_USE_TICKS = new HashMap<>();

    public PortalLinkingCard() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (!player.isCrouching()) {
            return InteractionResultHolder.pass(heldItem);
        }

        // Never consume crouch-use on the client; let server process authoritative targeting.
        if (level.isClientSide) {
            return InteractionResultHolder.pass(heldItem);
        }

        // Only allow clearing when crouch-using in open air, not while targeting a block.
        BlockHitResult blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = blockHitResult.getBlockPos();
            BlockState hitState = level.getBlockState(hitPos);

            // Handle endpoint crouch-interactions here so unlinking does not depend on block routing order.
            if (isPortalEndpoint(hitState)) {
                InteractionResult interaction = useOnPortalEndpoint(level, player, hitPos);
                if (interaction == InteractionResult.FAIL) {
                    return InteractionResultHolder.fail(heldItem);
                }
                if (interaction.consumesAction()) {
                    return InteractionResultHolder.success(heldItem);
                }
            }
            return InteractionResultHolder.pass(heldItem);
        }


        CompoundTag tag = heldItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag portalData = tag.getCompound(PORTAL_DATA_TAG);

        if (portalData.isEmpty()) {
            player.displayClientMessage(Component.translatable("interaction.custom_gateways.portal_linking_card.clear.empty"), true);
            return InteractionResultHolder.success(heldItem);
        }

        clearStoredPortalData(heldItem, tag);
        level.playSound(null, player.blockPosition(), SoundRegistry.LINKING_CARD_UNLINK_PORTAL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.displayClientMessage(Component.translatable("interaction.custom_gateways.portal_linking_card.clear.success"), true);
        return InteractionResultHolder.success(heldItem);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag portalData = tag.getCompound(PORTAL_DATA_TAG);

        if (portalData.isEmpty()) {
            // No source stored — card is empty
            tooltipComponents.add(Component.translatable("tooltip.custom_gateways.portal_linking_card.empty.status"));
            tooltipComponents.add(Component.translatable("tooltip.custom_gateways.portal_linking_card.empty.action"));
        } else {
            // Source portal is stored
            String dimension = portalData.getString(DIMENSION_TAG);
            int x = portalData.getInt(X_TAG);
            int y = portalData.getInt(Y_TAG);
            int z = portalData.getInt(Z_TAG);

            // Shorten the dimension name (e.g. "minecraft:overworld" → "overworld")
            String dimShort = dimension.contains(":") ? dimension.split(":")[1] : dimension;

            tooltipComponents.add(Component.translatable("tooltip.custom_gateways.portal_linking_card.linked.status"));
            tooltipComponents.add(Component.translatable("tooltip.custom_gateways.portal_linking_card.linked.dimension", dimShort));
            tooltipComponents.add(Component.translatable("tooltip.custom_gateways.portal_linking_card.linked.position", x, y, z));
            tooltipComponents.add(Component.translatable("tooltip.custom_gateways.portal_linking_card.linked.action"));
            tooltipComponents.add(Component.translatable("tooltip.custom_gateways.portal_linking_card.unlink_action"));
        }
    }

    /**
     * Called when the card is used on a valid portal endpoint block
     */
    public static InteractionResult useOnPortalEndpoint(Level level, Player player, BlockPos portalPos) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof PortalLinkingCard)) {
            return InteractionResult.FAIL;
        }

        // Read current custom data from card (1.21.1 DataComponents API)
        CompoundTag tag = heldItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag portalData = tag.getCompound(PORTAL_DATA_TAG);
        ResourceLocation currentDimension = level.dimension().location();
        ServerLevel serverLevel = (ServerLevel) level;
        PortalRegistry registry = PortalRegistry.get(serverLevel);

        BlockState currentState = level.getBlockState(portalPos);
        if (!isPortalEndpoint(currentState)) {
            return InteractionResult.FAIL;
        }

        PortalRegistry.PortalLocation currentPortal = new PortalRegistry.PortalLocation(currentDimension, portalPos);
        if (player.isCrouching()) {
            if (!isDoubleCrouchUse(serverLevel, player)) {
                player.displayClientMessage(Component.translatable("interaction.custom_gateways.portal_linking_card.unlink.double_crouch_hint"), true);
                return InteractionResult.SUCCESS;
            }

            PortalRegistry.PortalLocation linkedPortal = registry.getLinkedPortal(currentPortal);
            if (linkedPortal == null) {
                player.displayClientMessage(Component.translatable("interaction.custom_gateways.portal_linking_card.unlink.error.not_linked"), true);
                return InteractionResult.FAIL;
            }

            registry.removePortal(currentPortal);
            clearLinkState(level, portalPos);
            clearLinkState(resolveLevel(serverLevel, linkedPortal.dimension()), linkedPortal.getBlockPos());

            PortalRegistry.PortalLocation storedSource = getStoredSourceLocation(portalData);
            if (storedSource != null && (storedSource.equals(currentPortal) || storedSource.equals(linkedPortal))) {
                clearStoredPortalData(heldItem, tag);
            }

            level.playSound(null, portalPos, SoundRegistry.LINKING_CARD_UNLINK_PORTAL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

            player.displayClientMessage(
                Component.translatable(
                    "interaction.custom_gateways.portal_linking_card.unlink.success",
                    portalPos.toShortString(),
                    linkedPortal.getBlockPos().toShortString()
                ),
                false
            );
            return InteractionResult.SUCCESS;
        }

        // Check if we already have a source portal stored
        if (portalData.isEmpty()) {
            if (!isPortalLinkSource(currentState)) {
                player.displayClientMessage(Component.translatable("interaction.custom_gateways.portal_linking_card.error.source_must_be_frame"), true);
                return InteractionResult.FAIL;
            }

            // First click - store this portal as the source
            CompoundTag newPortalData = new CompoundTag();
            newPortalData.putString(DIMENSION_TAG, currentDimension.toString());
            newPortalData.putInt(X_TAG, portalPos.getX());
            newPortalData.putInt(Y_TAG, portalPos.getY());
            newPortalData.putInt(Z_TAG, portalPos.getZ());

            tag.put(PORTAL_DATA_TAG, newPortalData);
            heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            level.playSound(null, portalPos, SoundRegistry.LINKING_CARD_SET_SOURCE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

            player.displayClientMessage(
                Component.translatable(
                        "interaction.custom_gateways.portal_linking_card.set_source",
                        portalPos.toShortString(),
                        currentDimension.getPath()),
                true
            );
        } else {
            if (!isPortalLinkDestination(currentState)) {
                player.displayClientMessage(Component.translatable("interaction.custom_gateways.portal_linking_card.error.invalid_destination"), true);
                return InteractionResult.FAIL;
            }

            // Second click - link this portal to the stored source
            ResourceLocation sourceDimension = ResourceLocation.parse(portalData.getString(DIMENSION_TAG));
            int sourceX = portalData.getInt(X_TAG);
            int sourceY = portalData.getInt(Y_TAG);
            int sourceZ = portalData.getInt(Z_TAG);

            BlockPos sourcePos = new BlockPos(sourceX, sourceY, sourceZ);
            Level sourceLevel = resolveLevel(serverLevel, sourceDimension);
            BlockState sourceState = sourceLevel.getBlockState(sourcePos);
            if (!isPortalLinkSource(sourceState)) {
                clearStoredPortalData(heldItem, tag);
                player.displayClientMessage(Component.translatable("interaction.custom_gateways.portal_linking_card.error.stored_source_invalid"), true);
                return InteractionResult.FAIL;
            }

            // Check if we're linking to the same portal
            if (sourcePos.equals(portalPos) && sourceDimension.equals(currentDimension)) {
                player.displayClientMessage(Component.translatable("interaction.custom_gateways.portal_linking_card.error.self_link"), true);
                return InteractionResult.FAIL;
            }

            // Create the bidirectional link
            PortalRegistry.PortalLocation source = new PortalRegistry.PortalLocation(sourceDimension, sourcePos);
            PortalRegistry.PortalLocation destination = new PortalRegistry.PortalLocation(currentDimension, portalPos);

            // Explicit unlink is required before relinking either endpoint.
            if (registry.getLinkedPortal(source) != null || registry.getLinkedPortal(destination) != null) {
                player.displayClientMessage(Component.translatable("interaction.custom_gateways.portal_linking_card.error.already_linked"), true);
                return InteractionResult.FAIL;
            }

            registry.linkPortals(source, destination);

            // Update both block entities immediately so linked state + animation are visible now.
            triggerLinkStateUpdate(level, sourcePos, destination);
            triggerLinkStateUpdate(level, portalPos, source);

            clearStoredPortalData(heldItem, tag);

            level.playSound(null, portalPos, SoundRegistry.LINKING_CARD_LINK_PORTAL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

            CriterionRegistry.LINK_PORTALS.get().trigger((ServerPlayer) player);

            player.displayClientMessage(
                Component.translatable("interaction.custom_gateways.portal_linking_card.link_portal", sourcePos.toShortString(), portalPos.toShortString()),
                false
            );

        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isDoubleCrouchUse(ServerLevel level, Player player) {
        UUID playerId = player.getUUID();
        long now = level.getGameTime();
        Long lastUse = LAST_CROUCH_USE_TICKS.get(playerId);

        if (lastUse != null && now - lastUse <= DOUBLE_CROUCH_WINDOW_TICKS) {
            LAST_CROUCH_USE_TICKS.remove(playerId);
            return true;
        }

        LAST_CROUCH_USE_TICKS.put(playerId, now);
        return false;
    }

    private static void clearStoredPortalData(ItemStack heldItem, CompoundTag tag) {
        tag.remove(PORTAL_DATA_TAG);
        heldItem.set(DataComponents.CUSTOM_DATA, tag.isEmpty() ? CustomData.EMPTY : CustomData.of(tag));
    }

    private static PortalRegistry.PortalLocation getStoredSourceLocation(CompoundTag portalData) {
        if (portalData.isEmpty()) {
            return null;
        }

        ResourceLocation sourceDimension = ResourceLocation.parse(portalData.getString(DIMENSION_TAG));
        BlockPos sourcePos = new BlockPos(portalData.getInt(X_TAG), portalData.getInt(Y_TAG), portalData.getInt(Z_TAG));
        return new PortalRegistry.PortalLocation(sourceDimension, sourcePos);
    }

    private static boolean isPortalEndpoint(BlockState state) {
        return isPortalLinkSource(state) || isPortalLinkDestination(state);
    }

    private static boolean isPortalLinkSource(BlockState state) {
        return state.is(PortalLinkTags.PORTAL_LINK_SOURCES);
    }

    private static boolean isPortalLinkDestination(BlockState state) {
        return state.is(PortalLinkTags.PORTAL_LINK_DESTINATIONS);
    }

    private static Level resolveLevel(ServerLevel currentLevel, ResourceLocation dimension) {
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimension);
        ServerLevel resolved = currentLevel.getServer().getLevel(dimensionKey);
        return resolved != null ? resolved : currentLevel;
    }

    private static void triggerLinkStateUpdate(Level level, BlockPos portalPos, PortalRegistry.PortalLocation target) {
        BlockEntity blockEntity = level.getBlockEntity(portalPos);
        if (blockEntity instanceof PortalFrameEntity portalFrameEntity) {
            portalFrameEntity.setLinkedTarget(target.dimension(), target.getBlockPos(), true);
        } else if (blockEntity instanceof PortalPadEntity portalPadEntity) {
            portalPadEntity.setLinked(true);
        }
    }

    private static void clearLinkState(Level level, BlockPos portalPos) {
        BlockEntity blockEntity = level.getBlockEntity(portalPos);
        if (blockEntity instanceof PortalFrameEntity portalFrameEntity) {
            portalFrameEntity.clearLinkedTarget();
        } else if (blockEntity instanceof PortalPadEntity portalPadEntity) {
            portalPadEntity.setLinked(false);
        }
    }
}
