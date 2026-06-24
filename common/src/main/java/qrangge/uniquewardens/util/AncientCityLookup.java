package qrangge.uniquewardens.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

public final class AncientCityLookup {
    private AncientCityLookup() {}

    // Finds the current city center as a utility for ritual handler.
    public static BlockPos findCityCenter(ServerLevel level, BlockPos pos) {
        var structureRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var ancientCity = structureRegistry.getOrThrow(BuiltinStructures.ANCIENT_CITY);
        var start = level.structureManager().getStructureAt(pos, ancientCity.value());

        return start.isValid() ? start.getBoundingBox().getCenter() : null;
    }
}