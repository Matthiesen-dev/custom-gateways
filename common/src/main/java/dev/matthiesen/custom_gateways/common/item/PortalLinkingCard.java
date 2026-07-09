package dev.matthiesen.custom_gateways.common.item;

import dev.matthiesen.custom_gateways.common.data.PortalRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;
public final class PortalLinkingCard extends Item {
    public static final String PORTAL_DATA_TAG = "portal_data";
    public static final String DIMENSION_TAG = "dimension";
    public static final String X_TAG = "x";
    public static final String Y_TAG = "y";
    public static final String Z_TAG = "z";

    public PortalLinkingCard() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag portalData = tag.getCompound(PORTAL_DATA_TAG);

        if (portalData.isEmpty()) {
            // No source stored — card is empty
            tooltipComponents.add(Component.literal("§7Status: §cNo source portal set"));
            tooltipComponents.add(Component.literal("§7Right-click a §6Portal Frame §7to set a source."));
        } else {
            // Source portal is stored
            String dimension = portalData.getString(DIMENSION_TAG);
            int x = portalData.getInt(X_TAG);
            int y = portalData.getInt(Y_TAG);
            int z = portalData.getInt(Z_TAG);

            // Shorten the dimension name (e.g. "minecraft:overworld" → "overworld")
            String dimShort = dimension.contains(":") ? dimension.split(":")[1] : dimension;

            tooltipComponents.add(Component.literal("§7Status: §aSource portal set"));
            tooltipComponents.add(Component.literal("§7Dimension: §b" + dimShort));
            tooltipComponents.add(Component.literal("§7Position: §e" + x + ", " + y + ", " + z));
            tooltipComponents.add(Component.literal("§7Right-click a §6Portal Frame §7to link."));
        }
    }

    /**
     * Called when the card is used on a portal frame block
     */
    public static InteractionResult useOnPortalFrame(Level level, Player player, BlockPos portalPos) {
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

        // Check if we already have a source portal stored
        if (portalData.isEmpty()) {
            // First click - store this portal as the source
            CompoundTag newPortalData = new CompoundTag();
            newPortalData.putString(DIMENSION_TAG, currentDimension.toString());
            newPortalData.putInt(X_TAG, portalPos.getX());
            newPortalData.putInt(Y_TAG, portalPos.getY());
            newPortalData.putInt(Z_TAG, portalPos.getZ());

            tag.put(PORTAL_DATA_TAG, newPortalData);
            heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            player.displayClientMessage(
                Component.literal("§6Linking Card: §eSource portal set at §b" +
                    portalPos.toShortString() + " §ein dimension §b" + currentDimension.getPath()),
                true
            );
            return InteractionResult.SUCCESS;
        } else {
            // Second click - link this portal to the stored source
            ResourceLocation sourceDimension = ResourceLocation.parse(portalData.getString(DIMENSION_TAG));
            int sourceX = portalData.getInt(X_TAG);
            int sourceY = portalData.getInt(Y_TAG);
            int sourceZ = portalData.getInt(Z_TAG);

            BlockPos sourcePos = new BlockPos(sourceX, sourceY, sourceZ);

            // Check if we're linking to the same portal
            if (sourcePos.equals(portalPos) && sourceDimension.equals(currentDimension)) {
                player.displayClientMessage(Component.literal("§cCannot link a portal to itself!"), true);
                return InteractionResult.FAIL;
            }

            // Create the bidirectional link
            PortalRegistry.PortalLocation source = new PortalRegistry.PortalLocation(sourceDimension, sourcePos);
            PortalRegistry.PortalLocation destination = new PortalRegistry.PortalLocation(currentDimension, portalPos);

            // Get the portal registry from server level data
            PortalRegistry registry = PortalRegistry.getInstance();

            registry.linkPortals(source, destination);

            // Clear the stored portal data from the card
            tag.remove(PORTAL_DATA_TAG);
            if (tag.isEmpty()) {
                heldItem.remove(DataComponents.CUSTOM_DATA);
            } else {
                heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }

            player.displayClientMessage(
                Component.literal("§6Portals linked! §aSource: §b" + sourcePos.toShortString() +
                    " §adest: §b" + portalPos.toShortString()),
                false
            );

            return InteractionResult.SUCCESS;
        }
    }

    /**
     * Gets the stored portal location from the card, or null if none stored
     */
    public static PortalRegistry.PortalLocation getStoredPortal(ItemStack card) {
        if (!(card.getItem() instanceof PortalLinkingCard)) {
            return null;
        }

        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(PORTAL_DATA_TAG)) {
            return null;
        }

        CompoundTag portalData = tag.getCompound(PORTAL_DATA_TAG);
        if (portalData.isEmpty()) {
            return null;
        }

        try {
            ResourceLocation dimension = ResourceLocation.parse(portalData.getString(DIMENSION_TAG));
            int x = portalData.getInt(X_TAG);
            int y = portalData.getInt(Y_TAG);
            int z = portalData.getInt(Z_TAG);
            return new PortalRegistry.PortalLocation(dimension, x, y, z);
        } catch (Exception e) {
            return null;
        }
    }
}
