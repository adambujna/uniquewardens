package qrangge.uniquewardens.uniquewardens;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

public class FabricUniqueWardensData extends SavedData {
    public static final String ID = "uniquewardens";

    // Saves central coordinates of cleared cities from level.structureManager().getStructureAt(pos, Structure)
    private final LongOpenHashSet clearedCities;    // Cities where Warden was defeated
    private final LongOpenHashSet activeWardens;    // Cities where Warden currently spawned

    public FabricUniqueWardensData() {
        this(new LongOpenHashSet(), new LongOpenHashSet());
    }

    public FabricUniqueWardensData(LongOpenHashSet clearedCities, LongOpenHashSet activeWardens) {
        this.clearedCities = clearedCities;
        this.activeWardens = activeWardens;
    }

    // Replaces SavedDataType TYPE
    public static final SavedData.Factory<FabricUniqueWardensData> FACTORY =
            new SavedData.Factory<>(FabricUniqueWardensData::new, FabricUniqueWardensData::load, null);

    public static FabricUniqueWardensData load(CompoundTag tag, HolderLookup.Provider registries) {
        return new FabricUniqueWardensData(
                readSet(tag, "ClearedCities"),
                readSet(tag, "ActiveWardens"));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLongArray("ClearedCities", clearedCities.toLongArray());
        tag.putLongArray("ActiveWardens", activeWardens.toLongArray());
        return tag;
    }

    private static LongOpenHashSet readSet(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_LONG_ARRAY)
                ? new LongOpenHashSet(tag.getLongArray(key))
                : new LongOpenHashSet();
    }

    // Check if city's Warden was killed
    public boolean isCleared(long pos) {
        return clearedCities.contains(pos);
    }
    // Mark city as cleared when its Warden is killed
    public void markCleared(long pos) {
        boolean removed = activeWardens.remove(pos);
        boolean added = clearedCities.add(pos);
        if (removed || added) {
            setDirty();
        }
    }
    // See if city currently has a Warden spawned
    public boolean isWardenAlive(long pos) {
        return activeWardens.contains(pos);
    }
    // Mark city as currently having a Warden spawned
    public void markWardenSpawned(long pos) {
        if (activeWardens.add(pos)) {
            setDirty();
        }
    }
    // Remove Warden spawned when it despawns
    public void removeWardenSpawned(long pos) {
        if (activeWardens.remove(pos)) {
            setDirty();
        }
    }
    public void removeCleared(long pos) {
        if (clearedCities.remove(pos)) {
            setDirty();
        }
    }
}
