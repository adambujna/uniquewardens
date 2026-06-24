package qrangge.uniquewardens.services.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;


public interface IDataHelper {

    // Check if city's Warden has been killed
    boolean isCityCleared(ServerLevel level, BlockPos pos);

    // Mark city as cleared
    void markCityCleared(ServerLevel level, BlockPos pos);

    // Unmark city as cleared (Warden respawn through ritual)
    void removeCityCleared(ServerLevel level, BlockPos pos);

    // Check if city currently has spawned Warden
    boolean isCityWardenAlive(ServerLevel level, BlockPos cityOrigin);

    // Mark city as having spawned Warden
    void markCityWardenAlive(ServerLevel level, BlockPos cityOrigin);

    // Mark city as having a currently inactive despawned warden but not cleared
    void removeCityWardenAlive(ServerLevel level, BlockPos cityOrigin);
}