package qrangge.uniquewardens.uniquewardens;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import qrangge.uniquewardens.services.helpers.IDataHelper;

public class NeoForgeDataHelper implements IDataHelper {
    @SuppressWarnings("resource")
    private NeoForgeUniqueWardensData getData(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(NeoForgeUniqueWardensData.TYPE);
    }

    @Override
    public boolean isCityCleared(ServerLevel level, BlockPos pos) {
        return getData(level).isCleared(pos.asLong());
    }

    @Override
    public void markCityCleared(ServerLevel level, BlockPos pos) {
        getData(level).markCleared(pos.asLong());
    }

    @Override
    public boolean isCityWardenAlive(ServerLevel level, BlockPos cityOrigin) {
        return getData(level).isWardenAlive(cityOrigin.asLong());
    }

    @Override
    public void markCityWardenAlive(ServerLevel level, BlockPos cityOrigin) {
        getData(level).markWardenSpawned(cityOrigin.asLong());
    }

    @Override
    public void removeCityWardenAlive(ServerLevel level, BlockPos cityOrigin) {
        getData(level).removeWardenSpawned(cityOrigin.asLong());
    }

    @Override
    public void removeCityCleared(ServerLevel level, BlockPos cityOrigin) {
        getData(level).removeCleared(cityOrigin.asLong());
    }
}
