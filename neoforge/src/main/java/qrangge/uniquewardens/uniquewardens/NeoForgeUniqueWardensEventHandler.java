package qrangge.uniquewardens.uniquewardens;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import qrangge.uniquewardens.events.UniqueWardensEventHandler;
import qrangge.uniquewardens.ritual.RitualSequence;
import qrangge.uniquewardens.ritual.RitualWardenSpawn;

public class NeoForgeUniqueWardensEventHandler {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(NeoForgeUniqueWardensEventHandler::onEntityLeaveLevel);
        NeoForge.EVENT_BUS.addListener(NeoForgeUniqueWardensEventHandler::onLivingDeath);

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> RitualWardenSpawn.tick());
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> RitualSequence.tick());
    }

    private static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof Warden warden)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        if (warden.getRemovalReason() == Entity.RemovalReason.DISCARDED) {
            UniqueWardensEventHandler.onWardenDiscarded(warden, level);
        }
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Warden warden)) return;
        if (!(warden.level() instanceof ServerLevel level)) return;

        UniqueWardensEventHandler.onWardenKilled(warden, level);
    }
}
