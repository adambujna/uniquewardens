package qrangge.uniquewardens.uniquewardens;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import qrangge.uniquewardens.Constants;


public class FabricUniqueWardensData extends SavedData {
    // Saves central coordinates of cleared cities from level.structureManager().getStructureAt(pos, Structure)
    private final LongOpenHashSet clearedCities;    // Cities where Warden was defeated
    private final LongOpenHashSet activeWardens;    // Cities where Warden currently spawned

    // Constructor Codec uses for immutability
    public FabricUniqueWardensData(LongOpenHashSet clearedCities,  LongOpenHashSet activeWardens) {
        this.clearedCities = clearedCities;
        this.activeWardens = activeWardens;
    }

    // Codec and data type for saving/loading
    public static final Codec<FabricUniqueWardensData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.LONG_STREAM.xmap(
                            s -> { LongOpenHashSet set = new LongOpenHashSet(); s.forEach(set::add); return set; },
                            LongOpenHashSet::longStream
                    ).fieldOf("ClearedCities").forGetter(d -> d.clearedCities),
                    Codec.LONG_STREAM.xmap(
                            s -> { LongOpenHashSet set = new LongOpenHashSet(); s.forEach(set::add); return set; },
                            LongOpenHashSet::longStream
                    ).fieldOf("ActiveWardens").forGetter(d -> d.activeWardens)
            ).apply(instance, FabricUniqueWardensData::new)
    );

    public static final SavedDataType<FabricUniqueWardensData> TYPE = new SavedDataType<FabricUniqueWardensData>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "uniquewardens"),
            () -> new FabricUniqueWardensData(new LongOpenHashSet(), new LongOpenHashSet()),
            CODEC,
            DataFixTypes.LEVEL
    );

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
