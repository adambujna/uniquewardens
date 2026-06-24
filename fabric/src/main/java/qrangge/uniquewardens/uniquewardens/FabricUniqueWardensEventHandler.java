package qrangge.uniquewardens.uniquewardens;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import qrangge.uniquewardens.events.UniqueWardensEventHandler;
import qrangge.uniquewardens.ritual.RitualSequence;
import qrangge.uniquewardens.ritual.RitualWardenSpawn;

public class FabricUniqueWardensEventHandler {
    public static void register() {
        // Fired when health hits zero, before removal.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof Warden warden && warden.level() instanceof ServerLevel level) {
                UniqueWardensEventHandler.onWardenKilled(warden, level);
            }
        });

        // Fired when entity leaves the level for any reason.
        // Only handle DISCARDED here, KILLED is already covered above.
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (!(entity instanceof Warden warden)) return;

            if (warden.getRemovalReason() == Entity.RemovalReason.DISCARDED) {
                UniqueWardensEventHandler.onWardenDiscarded(warden, level);
            }
            // UNLOADED_TO_CHUNK / UNLOADED_WITH_PLAYER = just a chunk boundary,
            // the Warden is still alive somewhere (and should reload with $homeCityPos), so we do nothing.
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> RitualWardenSpawn.tick());
        ServerTickEvents.END_SERVER_TICK.register(server -> RitualSequence.tick());
    }
}
