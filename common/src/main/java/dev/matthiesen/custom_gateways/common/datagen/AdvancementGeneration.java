package dev.matthiesen.custom_gateways.common.datagen;

import dev.matthiesen.custom_gateways.common.CustomGatewaysCommon;
import dev.matthiesen.custom_gateways.common.advancements.SimpleTriggerCriterion;
import dev.matthiesen.custom_gateways.common.registry.BlockRegistry;
import dev.matthiesen.custom_gateways.common.registry.CriterionRegistry;
import dev.matthiesen.custom_gateways.common.registry.ItemRegistry;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class AdvancementGeneration {
    public static void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
        ItemStack portalFrameIcon = new ItemStack(BlockRegistry.PORTAL_FRAME.get());
        ItemStack portalLinkingDeviceIcon = new ItemStack(ItemRegistry.PORTAL_LINKING_CARD.get());
        ItemStack remoteDialerIcon = new ItemStack(ItemRegistry.REMOTE_DIALER.get());
        ItemStack portalPadIcon = new ItemStack(BlockRegistry.PORTAL_PAD.get());
        ItemStack portalStoneIcon = new ItemStack(BlockRegistry.PORTAL_STONE.get());
        ItemStack ancientPortalIcon = new ItemStack(BlockRegistry.ANCIENT_PORTAL.get());
        ItemStack netherGateIcon = new ItemStack(BlockRegistry.NETHER_GATE.get());

        AdvancementHolder root = createRootAdvancement(
                portalFrameIcon,
                label("root.title"),
                label("root.description"),
                AdvancementType.TASK,
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
                "tick", CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty())),
                consumer,
                "root"
        );

        AdvancementHolder craftedPortalLinkingDevice = createChildAdvancement(
                portalLinkingDeviceIcon,
                label("crafted.portal_linking_device.title"),
                label("crafted.portal_linking_device.description"),
                AdvancementType.GOAL,
                root,
                "requirement", createInventoryChangeCriterion(new ItemStack(ItemRegistry.PORTAL_LINKING_CARD.get())),
                consumer,
                "crafted/portal_linking_device"
        );

        AdvancementHolder craftedPortalPad = createChildAdvancement(
                portalPadIcon,
                label("crafted.portal_pad.title"),
                label("crafted.portal_pad.description"),
                AdvancementType.TASK,
                craftedPortalLinkingDevice,
                "requirement", createInventoryChangeCriterion(new ItemStack(BlockRegistry.PORTAL_PAD.get())),
                consumer,
                "crafted/portal_pad"
        );

        AdvancementHolder craftedRemoteDialer = createChildAdvancement(
                remoteDialerIcon,
                label("crafted.remote_dialer.title"),
                label("crafted.remote_dialer.description"),
                AdvancementType.CHALLENGE,
                craftedPortalLinkingDevice,
                "requirement", createInventoryChangeCriterion(new ItemStack(ItemRegistry.REMOTE_DIALER.get())),
                consumer,
                "crafted/remote_dialer"
        );

        AdvancementHolder craftedPortalStone = createChildAdvancement(
                portalStoneIcon,
                label("crafted.portal_stone.title"),
                label("crafted.portal_stone.description"),
                AdvancementType.TASK,
                craftedPortalPad,
                "requirement", createInventoryChangeCriterion(new ItemStack(BlockRegistry.PORTAL_FRAME.get())),
                consumer,
                "crafted/portal_stone"
        );

        AdvancementHolder craftedPortalFrame = createChildAdvancement(
                portalFrameIcon,
                label("crafted.portal_frame.title"),
                label("crafted.portal_frame.description"),
                AdvancementType.TASK,
                craftedPortalPad,
                "requirement", createInventoryChangeCriterion(new ItemStack(BlockRegistry.PORTAL_FRAME.get())),
                consumer,
                "crafted/portal_frame"
        );

        AdvancementHolder craftedAncientPortal = createChildAdvancement(
                ancientPortalIcon,
                label("crafted.ancient_portal.title"),
                label("crafted.ancient_portal.description"),
                AdvancementType.TASK,
                craftedPortalPad,
                "requirement", createInventoryChangeCriterion(new ItemStack(BlockRegistry.ANCIENT_PORTAL.get())),
                consumer,
                "crafted/ancient_portal"
        );

        AdvancementHolder craftedNetherGate = createChildAdvancement(
                netherGateIcon,
                label("crafted.nether_gate.title"),
                label("crafted.nether_gate.description"),
                AdvancementType.TASK,
                craftedPortalPad,
                "requirement", createInventoryChangeCriterion(new ItemStack(BlockRegistry.NETHER_GATE.get())),
                consumer,
                "crafted/nether_gate"
        );

        AdvancementHolder linkedPortals = createChildAdvancement(
                portalFrameIcon,
                label("portals.linked.title"),
                label("portals.linked.description"),
                AdvancementType.TASK,
                craftedPortalLinkingDevice,
                "linked_portal", SimpleTriggerCriterion.Conditions.create(
                        CriterionRegistry.LINK_PORTALS.get()
                ),
                consumer,
                "portals/linked"
        );

        AdvancementHolder linkedCrossDimensionPortals = createChildAdvancement(
                portalFrameIcon,
                label("portals.linked_cross_dimension.title"),
                label("portals.linked_cross_dimension.description"),
                AdvancementType.CHALLENGE,
                linkedPortals,
                "linked_cross_dimension_portal", SimpleTriggerCriterion.Conditions.create(
                        CriterionRegistry.LINK_CROSS_DIMENSION_PORTALS.get()
                ),
                consumer,
                "portals/linked_cross_dimension"
        );

        AdvancementHolder usedPortal = createChildAdvancement(
                portalFrameIcon,
                label("portals.used.title"),
                label("portals.used.description"),
                AdvancementType.TASK,
                root,
                "used_portal", SimpleTriggerCriterion.Conditions.create(
                        CriterionRegistry.USE_PORTAL.get()
                ),
                consumer,
                "portals/used"
        );

        AdvancementHolder usedRemoteDialer = createChildAdvancement(
                remoteDialerIcon,
                label("portals.used_remote_dialer.title"),
                label("portals.used_remote_dialer.description"),
                AdvancementType.TASK,
                root,
                "used_remote_dialer", SimpleTriggerCriterion.Conditions.create(
                        CriterionRegistry.USE_REMOTE_DIALER.get()
                ),
                consumer,
                "portals/used_remote_dialer"
        );

    }

    private static Criterion<?> createInventoryChangeCriterion(ItemStack itemStack) {
        return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
                new InventoryChangeTrigger.TriggerInstance(
                        Optional.empty(),
                        InventoryChangeTrigger.TriggerInstance.Slots.ANY,
                        List.of(
                                net.minecraft.advancements.critereon.ItemPredicate.Builder.item()
                                        .of(itemStack.getItem())
                                        .build()
                        )
                )
        );
    }

    private static String label(String text) {
        return "advancements.custom_gateways." + text;
    }

    @SuppressWarnings("SameParameterValue")
    private static AdvancementHolder createRootAdvancement(
            ItemStack displayItem, String title, String description, AdvancementType type, ResourceLocation background,
            String criterionString, Criterion<?> criterion,
            Consumer<AdvancementHolder> consumer, String modResourceLocation
    ) {
        return Advancement.Builder.advancement()
                .display(
                        displayItem,
                        Component.translatable(title),
                        Component.translatable(description),
                        background,
                        type,
                        false, // Show the toast when completing it
                        false, // Announce it to chat
                        false // Hide it in the advancement tab until it's achieved
                )
                .addCriterion(criterionString, criterion)
                .save(consumer, CustomGatewaysCommon.modResourceFile(modResourceLocation));
    }

    private static AdvancementHolder createChildAdvancement(
            ItemStack displayItem, String title, String description, AdvancementType type,
            AdvancementHolder parent,
            String criterionString, Criterion<?> criterion,
            Consumer<AdvancementHolder> consumer, String modResourceLocation
    ) {
        return Advancement.Builder.advancement()
                .display(
                        displayItem,
                        Component.translatable(title),
                        Component.translatable(description),
                        null,
                        type,
                        true, // Show the toast when completing it
                        true, // Announce it to chat
                        false // Hide it in the advancement tab until it's achieved
                )
                .parent(parent)
                .addCriterion(criterionString, criterion)
                .save(consumer, CustomGatewaysCommon.modResourceFile(modResourceLocation));
    }
}
