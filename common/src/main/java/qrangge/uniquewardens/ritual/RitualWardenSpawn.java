package qrangge.uniquewardens.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import qrangge.uniquewardens.accessor.WardenAccessor;
import qrangge.uniquewardens.services.Services;

import java.util.ArrayList;
import java.util.List;

public class RitualWardenSpawn {
    private RitualWardenSpawn() {}

    private record PendingSpawn(ServerLevel level, BlockPos pos, BlockPos cityCenter, int ticksRemaining) {}

    private static final List<PendingSpawn> PENDING = new ArrayList<>();

    public static void schedule(ServerLevel level, BlockPos pos, BlockPos cityCenter, int delayTicks) {
        PENDING.add(new PendingSpawn(level, pos, cityCenter, delayTicks));
    }

    // Call once per server tick
    public static void tick() {
        if (PENDING.isEmpty()) return;

        List<PendingSpawn> next = new ArrayList<>();
        for (PendingSpawn p : PENDING) {
            int remaining = p.ticksRemaining() - 1;
            if (remaining <= 0) {
                executeSpawn(p.level(), p.pos(), p.cityCenter());
            } else {
                next.add(new PendingSpawn(p.level(), p.pos(), p.cityCenter(), remaining));
            }
        }
        PENDING.clear();
        PENDING.addAll(next);
    }

    // Spawns warden
    private static void executeSpawn(ServerLevel level, BlockPos pos, BlockPos cityCenter) {
        level.playSound(null, pos, SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 2.0f, 1.0f);
        level.playSound(null, pos, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 2.0f, 1.0f);
        SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, level, pos,
                        5, 5, 2, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER)
                .ifPresent(warden -> {
                    ((WardenAccessor) warden).uniquewardens$setHomeCity(cityCenter);
                    Services.DATA.removeCityCleared(level, cityCenter);
                    Services.DATA.markCityWardenAlive(level, cityCenter);
                });
    }
}
