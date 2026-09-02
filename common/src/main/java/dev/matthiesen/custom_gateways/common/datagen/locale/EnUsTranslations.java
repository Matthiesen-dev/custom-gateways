package dev.matthiesen.custom_gateways.common.datagen.locale;

import dev.matthiesen.custom_gateways.common.datagen.GlobalTranslations;
import dev.matthiesen.custom_gateways.common.datagen.util.TranslationBuilder;

public final class EnUsTranslations {
    private static final TranslationBuilder TRANSLATIONS = new TranslationBuilder("en_us");

    static {
        advancement("root.title", "Custom Gateways");
        advancement("root.description", "Discover the power of Custom Gateways, a mod that allows you to create and link portals for instant travel across dimensions.");
        advancement("crafted.portal_linking_device.title", "Crafted a Portal Linking Device");
        advancement("crafted.portal_linking_device.description", "Crafted a Portal Linking Device, an essential tool for linking Custom Gateways.");
        advancement("crafted.portal_frame.title", "Crafted a Portal Frame");
        advancement("crafted.portal_frame.description", "Crafted a Portal Frame, A gateway to new dimensions and adventures.");
        advancement("crafted.portal_pad.title", "Crafted a Portal Pad");
        advancement("crafted.portal_pad.description", "Crafted a Portal Pad, the foundation for your Custom Gateway.");
        advancement("crafted.ancient_portal.title", "Crafted an Ancient Portal");
        advancement("crafted.ancient_portal.description", "Crafted an Ancient Portal, a mysterious gateway to unknown realms.");
        advancement("crafted.portal_stone.title", "Crafted a Portal Stone");
        advancement("crafted.portal_stone.description", "Crafted a Portal Stone, A destination for your Custom Gateway.");
        advancement("crafted.nether_gate.title", "Crafted a Nether Gate");
        advancement("crafted.nether_gate.description", "Crafted a Nether Gate, a Demonic gateway");
        advancement("crafted.remote_dialer.title", "Crafted a Remote Dialer");
        advancement("crafted.remote_dialer.description", "Crafted a Remote Dialer, a device that allows you to open Custom Gateways remotely.");
        advancement("portals.used.title", "Used a Custom Gateway");
        advancement("portals.used.description", "Successfully used a Custom Gateway to teleport to another location.");
        advancement("portals.linked.title", "Linked Portals");
        advancement("portals.linked.description", "Successfully linked two portals together using the Portal Linking Device.");
        advancement("portals.linked_cross_dimension.title", "Interdimensional Gateway");
        advancement("portals.linked_cross_dimension.description", "Successfully linked two portals across different dimensions, such as the Overworld and the Nether.");
        advancement("portals.used_remote_dialer.title", "Used a Remote Dialer");
        advancement("portals.used_remote_dialer.description", "Successfully used a Remote Dialer to open a Custom Gateway from a distance.");

        block("portal_frame", "Portal Frame");
        block("portal_pad", "Portal Pad");
        block("ancient_portal", "Ancient Portal");
        block("portal_stone", "Portal Stone");
        block("nether_gate", "Nether Gate");
        block("remote_gateway", "Remote Gateway");

        item("portal_linking_device", "Portal Linking Device");
        item("remote_dialer", "Remote Dialer");

        itemGroup("portal_frame_tab", "Custom Gateways");
        itemGroup("portal_frame_tab.portal_frames_section", "Portal Frames");
        itemGroup("portal_frame_tab.portal_tools_section", "Portal Tools");

        jade("portal_frame", "Portal Frame");
        jade("portal_pad", "Portal Pad");
        jade("ancient_portal", "Ancient Portal");
        jade("portal_stone", "Portal Stone");
        jade("nether_gate", "Nether Gate");

        menu("remote_dialer.title", "Remote Dialer");
        menu("remote_dialer.open", "Open");
        menu("remote_dialer.delete", "Delete");
        menu("remote_dialer.rename", "Rename");
        menu("remote_dialer.revalidate", "Recheck");
        menu("remote_dialer.rename_input", "Entry name");
        menu("remote_dialer.rename_hint", "Select an entry, edit the name, then click Rename.");

        sound("gateway.teleport.success", "Gateway Teleport Success");
        sound("gateway.teleport.failure", "Gateway Teleport Failure");
        sound("linking_device.set_source", "Linking Device Set Source");
        sound("linking_device.link_portal", "Linking Device Link Portal");
        sound("linking_device.unlink_portal", "Linking Device Unlink Portal");

        interaction("portal_linking_device.set_source", "§6Linking Device: §eSource portal set at §b%s §ein dimension §b%s");
        interaction("portal_linking_device.link_portal", "§6Portals linked! §aSource: §b%s §aDest: §b%s");
        interaction("portal_linking_device.error.self_link", "§cCannot link a portal to itself!");
        interaction("portal_linking_device.error.already_linked", "§cOne of these portals is already linked. Unlink it before relinking.");
        interaction("portal_linking_device.error.source_must_be_frame", "§cSource must be a §6Portal Frame§c, §6Ancient Portal§c, or §6Nether Gate§c. Portal Pads and Portal Stones are destination-only.");
        interaction("portal_linking_device.error.invalid_destination", "§cThis block cannot be used as a link destination.");
        interaction("portal_linking_device.error.stored_source_invalid", "§cStored source is no longer a valid source endpoint. Source cleared.");
        interaction("portal_linking_device.unlink.double_crouch_hint", "§7Crouch-use again within §b3s §7to unlink this portal.");
        interaction("portal_linking_device.unlink.error.not_linked", "§cThis portal is not currently linked.");
        interaction("portal_linking_device.unlink.success", "§6Portal link removed! §aPortal: §b%s §aUnlinked from: §b%s");
        interaction("portal_linking_device.clear.empty", "§7Linking Device has no stored source to clear.");
        interaction("portal_linking_device.clear.success", "§6Linking Device source cleared.");
        interaction("ancient_portal.error.master_block", "Ancient Portal master block not found at %s");
        interaction("portal_frame.error.master_block", "Portal Frame master block not found at %s");
        interaction("portal_frame.telported", "§6Teleported to §e%s");
        interaction("remote_dialer.add.success", "§6Remote Dialer: Added §b%s");
        interaction("remote_dialer.add.duplicate", "§7Remote Dialer already has this location.");
        interaction("remote_dialer.add.full", "§cRemote Dialer is full (%s max).");
        interaction("remote_dialer.spawn.success", "§6Opening gateway to §b%s");
        interaction("remote_dialer.spawn.failed", "§cNo safe spot found for a remote gateway.");
        interaction("remote_dialer.select.failed", "§cThat entry cannot be opened right now.");
        interaction("remote_dialer.rename.failed", "§cRename failed. Enter a valid name.");

        tooltip("portal_linking_device.empty.status", "§7Status: §cNo source portal set");
        tooltip("portal_linking_device.empty.action", "§7Right-click a §6Portal Frame§7, §6Ancient Portal§7, or §6Nether Gate §7to set a source.");
        tooltip("portal_linking_device.linked.status", "§7Status: §aSource portal set");
        tooltip("portal_linking_device.linked.dimension", "§7Dimension: §b%s");
        tooltip("portal_linking_device.linked.position", "§7Position: §e%s, %s, %s");
        tooltip("portal_linking_device.linked.action", "§7Right-click a §6Portal Frame§7, §6Ancient Portal§7, §6Portal Pad§7, §6Portal Stone§7, or §6Nether Gate §7to link.");
        tooltip("portal_linking_device.unlink_action", "§7Crouch-use twice (within 3s) on a linked §6Portal Frame§7, §6Ancient Portal§7, §6Portal Pad§7, §6Portal Stone§7, or §6Nether Gate §7to unlink.");
        tooltip("remote_dialer.entries", "§7Saved destinations: §b%s§7/§b%s");
        tooltip("remote_dialer.invalid", "§7Invalid destinations: §c%s");
        tooltip("remote_dialer.open", "§7Right-click in air to open the dialer menu.");
        tooltip("remote_dialer.add", "§7Right-click a §6Portal Frame§7, §6Ancient Portal§7, §6Portal Pad§7, or §6Portal Stone §7to save that destination.");
        tooltip("ancient_portal.linked", "Ancient Portal is Linked");
        tooltip("ancient_portal.linked.position", "Position: %s");
        tooltip("ancient_portal.linked.dimension", "Dimension: %s");
        tooltip("ancient_portal.unlinked", "Ancient Portal is Unlinked");
        tooltip("portal_pad.destination_only", "§6Destination only: §7cannot be used as a teleport source.");
        tooltip("portal_stone.destination_only", "§6Destination only: §7cannot be used as a teleport source.");
        tooltip("portal_frame.linked", "Portal is Linked");
        tooltip("portal_frame.linked.position", "Position: %s");
        tooltip("portal_frame.linked.dimension", "Dimension: %s");
        tooltip("portal_frame.unlinked", "Portal is Unlinked");
        tooltip("nether_gate.linked", "Nether Gate is Linked");
        tooltip("nether_gate.unlinked", "Nether Gate is Unlinked");

        configuration("title", "Custom Gateways Configuration");
        configuration("section.custom.gateways.server.toml", "Server Configuration");
        configuration("section.custom.gateways.server.toml.title", "Server Configuration");
        configuration("server", "Server Configuration");
        configuration("server.tooltip", "Custom Gateways Server configuration.");
        configuration("server.button", "Edit");
        configuration("server.teleportValidation", "Teleport Validation Configuration");
        configuration("server.teleportValidation.tooltip", "Configuration for teleportation validation in Custom Gateways.");
        configuration("server.teleportValidation.button", "Edit");
        configuration("server.teleportValidation.cooldownMS", "Teleportation Cooldown (ms)");
        configuration("server.teleportValidation.cooldownMS.tooltip", "The cooldown in milliseconds between teleportation attempts.");
        configuration("server.teleportValidation.safeSearchRadius", "Safe Search Radius");
        configuration("server.teleportValidation.safeSearchRadius.tooltip", "The radius in blocks to search for a safe teleportation location.");
        configuration("server.teleportValidation.allowNonPlayerTeleport", "Allow Non-Player Teleportation");
        configuration("server.teleportValidation.allowNonPlayerTeleport.tooltip", "Whether to allow non-player entities to teleport using the custom gateways.");
        configuration("server.remoteDialer", "Remote Dialer Configuration");
        configuration("server.remoteDialer.tooltip", "Configuration for the Remote Dialer item in Custom Gateways.");
        configuration("server.remoteDialer.button", "Edit");
        configuration("server.remoteDialer.maxPortalEntries", "Max Portal Entries");
        configuration("server.remoteDialer.maxPortalEntries.tooltip", "The maximum number of portal entries that can be stored in a Remote Dialer item.");

    }

    private static void advancement(String key, String value) {
        TRANSLATIONS.addTranslation("advancements.custom_gateways." + key, value);
    }

    private static void block(String key, String value) {
        TRANSLATIONS.addTranslation("block.custom_gateways." + key, value);
    }

    private static void jade(String key, String value) {
        TRANSLATIONS.addTranslation("config.jade.plugin_custom_gateways." + key, value);
    }

    private static void interaction(String key, String value) {
        TRANSLATIONS.addTranslation("interaction.custom_gateways." + key, value);
    }

    private static void item(String key, String value) {
        TRANSLATIONS.addTranslation("item.custom_gateways." + key, value);
    }

    private static void itemGroup(String key, String value) {
        TRANSLATIONS.addTranslation("itemGroup.custom_gateways." + key, value);
    }

    private static void menu(String key, String value) {
        TRANSLATIONS.addTranslation("menu.custom_gateways." + key, value);
    }

    private static void sound(String key, String value) {
        TRANSLATIONS.addTranslation("sound.custom_gateways." + key, value);
    }

    private static void tooltip(String key, String value) {
        TRANSLATIONS.addTranslation("tooltip.custom_gateways." + key, value);
    }

    private static void configuration(String key, String value) {
        TRANSLATIONS.addTranslation("custom_gateways.configuration." + key, value);
    }

    public static void registerTranslations() {
        GlobalTranslations.addTranslations(TRANSLATIONS);
    }
}
