package qrangge.uniquewardens.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.warden.Warden;
import qrangge.uniquewardens.accessor.WardenAccessor;
import qrangge.uniquewardens.services.Services;

public final class UniqueWardensEventHandler {
    private UniqueWardensEventHandler() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // These methods are called when NeoForge/Fabric event bus registers Warden Entity DEATH/DISCARD event.
    //
    // Call when a Warden is KILLED (health reached zero).
    // Marks the home city as permanently cleared.
    public static void onWardenKilled(Warden warden, ServerLevel level) {
        BlockPos homeCity = ((WardenAccessor) warden).uniquewardens$getHomeCity();
        if (homeCity == null) return; // Not a city warden (e.g. spawned via egg)
        Services.DATA.markCityCleared(level, homeCity);
        Services.DATA.removeCityWardenAlive(level, homeCity);
    }

    // Call when a Warden is DESPAWNED/REMOVED.
    // Marks the city as capable of spawning again (unless cleared).
    public static void onWardenDiscarded(Warden warden, ServerLevel level) {
        BlockPos homeCity = ((WardenAccessor) warden).uniquewardens$getHomeCity();
        if (homeCity == null) return;
        Services.DATA.removeCityWardenAlive(level, homeCity);
    }
}
