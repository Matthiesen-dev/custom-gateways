package dev.matthiesen.custom_gateways.common.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Global registry for all portal links in the world.
 * Stored as SavedData in the world's dimension data folder.
 * Manages bidirectional portal links automatically.
 */
public final class PortalRegistry extends SavedData {
    public static final String NAME = "custom_gateways_portal_registry";
    public static final SavedData.Factory<PortalRegistry> FACTORY =
        new SavedData.Factory<>(PortalRegistry::new, PortalRegistry::load, null);

    // Maps portal location (dimension + coords) to its linked portal
    private final Map<PortalLocation, PortalLocation> portalLinks = new HashMap<>();

    public PortalRegistry() {
    }

    public static PortalRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    /**
     * Creates a bidirectional link between two portals
     */
    public void linkPortals(PortalLocation source, PortalLocation destination) {
        // Remove any existing links for these portals first
        removePortal(source);
        removePortal(destination);

        // Create bidirectional link
        portalLinks.put(source, destination);
        portalLinks.put(destination, source);
        setDirty();
    }

    /**
     * Gets the linked portal for a given portal location
     */
    @Nullable
    public PortalLocation getLinkedPortal(PortalLocation portalLocation) {
        return portalLinks.get(portalLocation);
    }

    /**
     * Removes a portal and its associated links
     */
    public void removePortal(PortalLocation portalLocation) {
        PortalLocation linked = portalLinks.remove(portalLocation);

        if (linked != null) {
            portalLinks.remove(linked);
            setDirty();
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();

        for (Map.Entry<PortalLocation, PortalLocation> entry : portalLinks.entrySet()) {
            CompoundTag tag = new CompoundTag();

            // Only save one direction to avoid duplication (bidirectional is recreated on load)
            PortalLocation source = entry.getKey();
            PortalLocation destination = entry.getValue();

            String sourceStr = source.toString();
            String destStr = destination.toString();

            if (sourceStr.compareTo(destStr) <= 0) {
                tag.put("source", source.serialize());
                tag.put("destination", destination.serialize());
                listTag.add(tag);
            }
        }

        compoundTag.put("portals", listTag);
        return compoundTag;
    }

    public static PortalRegistry load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        PortalRegistry registry = new PortalRegistry();
        ListTag listTag = compoundTag.getList("portals", Tag.TAG_COMPOUND);

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);
            PortalLocation source = PortalLocation.deserialize(tag.getCompound("source"));
            PortalLocation destination = PortalLocation.deserialize(tag.getCompound("destination"));
            registry.portalLinks.put(source, destination);
            registry.portalLinks.put(destination, source);
        }

        return registry;
    }

    /**
     * Represents a portal's location including its dimension
     */
    public static class PortalLocation {
        public final ResourceLocation dimension;
        public final int x;
        public final int y;
        public final int z;

        public PortalLocation(ResourceLocation dimension, int x, int y, int z) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public PortalLocation(ResourceLocation dimension, BlockPos pos) {
            this(dimension, pos.getX(), pos.getY(), pos.getZ());
        }

        public BlockPos getBlockPos() {
            return new BlockPos(x, y, z);
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("dimension", dimension.toString());
            tag.putInt("x", x);
            tag.putInt("y", y);
            tag.putInt("z", z);
            return tag;
        }

        public static PortalLocation deserialize(CompoundTag tag) {
            ResourceLocation dimension = ResourceLocation.parse(tag.getString("dimension"));
            int x = tag.getInt("x");
            int y = tag.getInt("y");
            int z = tag.getInt("z");
            return new PortalLocation(dimension, x, y, z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PortalLocation that)) return false;
            return x == that.x && y == that.y && z == that.z && dimension.equals(that.dimension);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimension, x, y, z);
        }

        @Override
        public String toString() {
            return dimension + ":" + x + "," + y + "," + z;
        }
    }
}
