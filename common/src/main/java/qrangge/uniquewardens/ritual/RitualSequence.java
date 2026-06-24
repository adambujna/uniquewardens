package qrangge.uniquewardens.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import qrangge.uniquewardens.registry.ModParticles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static qrangge.uniquewardens.ritual.RitualHandler.CROSS;

// Handles the staggered "closing in" effect once a ritual pattern is completed by lighting a candle.
// Torches break from a radius of 40 blocks inward over time
// If the altar (redstone block) survives until the countdown ends, the Warden digs out and city is reactivated.
// Breaking the altar mid-ritual cancels it with no other side effects.
public class RitualSequence {
    private RitualSequence() {}

    private static final int DURATION_TICKS = 15 * 20;   // total ritual length: 15 seconds
    private static final double MAX_RADIUS = 40.0;       // horizontal radius (blocks) the wave starts at
    private static final int Y_RANGE = 16;               // vertical scan range, +/- from altar height
    private static final int DARKNESS_DURATION_TICKS = 10 * 20; // how long darkness is applied for after spawn
    private static final int WARDEN_SPAWN_DELAY = 60;    // Delay after ritual is complete until warden spawns

    private static final List<ActiveRitual> ACTIVE = new ArrayList<>();

    public static void start(ServerLevel level, BlockPos center, BlockPos cityCenter) {
        boolean alreadyActive = ACTIVE.stream()
                .anyMatch(r -> r.level == level && r.center.equals(center));
        if (alreadyActive) return;

        List<TorchEntry> torches = collectTorches(level, center);
        torches.sort((a, b) -> Double.compare(b.distance, a.distance));
        ACTIVE.add(new ActiveRitual(level, center, cityCenter, torches));
    }

    // Call once per server tick
    // Tracks whether ritual block configuration was not disrupted
    public static void tick() {
        if (ACTIVE.isEmpty()) return;

        List<ActiveRitual> stillActive = new ArrayList<>();
        for (ActiveRitual ritual : ACTIVE) {
            if (!processTick(ritual)) {
                stillActive.add(ritual);
            }
        }
        ACTIVE.clear();
        ACTIVE.addAll(stillActive);
    }

    // Returns true if ritual is finished (completed or disrupted)
    private static boolean processTick(ActiveRitual ritual) {
        // Check correct block configuration
        if (!ritual.level.getBlockState(ritual.center).is(Blocks.REDSTONE_BLOCK)) {
            onCancelled(ritual);
            return true;
        }
        for (BlockPos offset : CROSS) {
            BlockPos sandPos = ritual.center.offset(offset);
            if (!ritual.level.getBlockState(sandPos).is(Blocks.SOUL_SAND)) {
                onCancelled(ritual);
                return true;
            }
            BlockState candle = ritual.level.getBlockState(sandPos.above());
            if (!candle.is(Blocks.CANDLE)
                    || candle.getValue(BlockStateProperties.LIT)
                    || candle.getValue(BlockStateProperties.CANDLES) != 1) {
                onCancelled(ritual);
                return true;
            }
        }
        // Spawn soul fire particles.
        if (ritual.level.getRandom().nextInt(8) == 0) {
            spawnCandleFireParticles(ritual);
        }

        ritual.ticksElapsed++;
        double progress = Math.min(1.0, (double) ritual.ticksElapsed / DURATION_TICKS);
        double eased = progress * progress * progress * progress; // power function ease-in: slow start, fast finish
        double currentRadius = MAX_RADIUS * (1.0 - eased);

        // Pop torches off the sorted list as the shrinking radius passes their distance.
        // No re-scanning needed so this is the only per-tick cost.
        while (ritual.torchIndex < ritual.torches.size()
                && ritual.torches.get(ritual.torchIndex).distance >= currentRadius) {
            BlockPos pos = ritual.torches.get(ritual.torchIndex).pos;
            if (ritual.level.isLoaded(pos)) {
                // Destroy torches
                if (isTorch(ritual.level.getBlockState(pos))) {
                    ritual.level.destroyBlock(pos, true);
                }
                else {
                    BlockState candle = ritual.level.getBlockState(pos);
                    // Extinguish candles
                    if (isCandle(candle) && candle.getValue(BlockStateProperties.LIT)) {
                        BlockState extinguished = candle.setValue(BlockStateProperties.LIT, false);
                        ritual.level.setBlock(pos, extinguished, 3);
                    }
                }

            }
            ritual.torchIndex++;
        }

        if (ritual.ticksElapsed >= DURATION_TICKS) {
            onComplete(ritual);
            return true;
        }
        return false;
    }

    private static void onComplete(ActiveRitual ritual) {
        ServerLevel level = ritual.level;
        BlockPos center = ritual.center;

        // Transforms the pattern into Sculk and a Catalyst.
        // Keeps catalysts farmable just like they were when they dropped from the Warden.
        level.setBlock(center, Blocks.SCULK_CATALYST.defaultBlockState(), 3);
        for (BlockPos offset : CROSS) {
            BlockPos sandPos = center.offset(offset);
            level.setBlock(sandPos, Blocks.SCULK.defaultBlockState(), 3);
            BlockState candle = ritual.level.getBlockState(sandPos.above());
            // Extinguish candles
            if (candle.is(Blocks.CANDLE) && candle.getValue(BlockStateProperties.LIT)) {
                BlockState extinguished = candle.setValue(BlockStateProperties.LIT, false);
                ritual.level.setBlock(sandPos.above(), extinguished, 3);
            }
            // Remove artificial light
            level.removeBlock(sandPos.above().above(), false);
        }

        level.playSound(null, center, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 2.0f, 0.5f);
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, new AABB(center).inflate(40))) {
            p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_DURATION_TICKS,
                    0, false, false));
        }

        RitualWardenSpawn.schedule(level, center, ritual.cityCenter, WARDEN_SPAWN_DELAY);
    }

    private static void onCancelled(ActiveRitual ritual) {
        ritual.level.playSound(null, ritual.center, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 1.0f, 1.0f);
        // Extinguish candles
        for (BlockPos offset : CROSS) {
            BlockPos candlePos = ritual.center.offset(offset).above();
            BlockState candle = ritual.level.getBlockState(candlePos);
            if (candle.is(Blocks.CANDLE) && candle.getValue(BlockStateProperties.LIT)) {
                BlockState extinguished = candle.setValue(BlockStateProperties.LIT, false);
                ritual.level.setBlock(candlePos, extinguished, 3);
            }
            // Remove artificial light
            ritual.level.removeBlock(candlePos.above(), false);
        }
    }

    private static List<TorchEntry> collectTorches(ServerLevel level, BlockPos center) {
        List<TorchEntry> result = new ArrayList<>();
        int r = (int) Math.ceil(MAX_RADIUS);
        double radiusSq = MAX_RADIUS * MAX_RADIUS;

        // The ritual's own 4 candles must never be swept up - they need to stay lit
        // for processTick()'s pattern check, otherwise the ritual interrupts itself
        Set<BlockPos> ritualCandles = new HashSet<>();
        for (BlockPos offset : CROSS) {
            ritualCandles.add(center.offset(offset).above());
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double horizDistSq = (double) x * x + (double) z * z;
                if (horizDistSq > radiusSq) continue;

                for (int y = -Y_RANGE; y <= Y_RANGE; y++) {
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (!level.isLoaded(cursor)) continue;
                    if (ritualCandles.contains(cursor)) continue; // Exclude altar candles

                    if (isTorch(level.getBlockState(cursor)) || isCandle(level.getBlockState(cursor))) {
                        result.add(new TorchEntry(cursor.immutable(), Math.sqrt(horizDistSq)));
                    }
                }
            }
        }
        return result;
    }

    // Spawns soul fire particles around a candle
    private static void spawnCandleFireParticles(ActiveRitual ritual) {
        RandomSource random = ritual.level.getRandom();
        for (BlockPos offset : CROSS) {
            BlockPos candlePos = ritual.center.offset(offset).above();
            if (!ritual.level.getBlockState(candlePos).is(Blocks.CANDLE)) continue;

            double x = candlePos.getX() + 0.5;
            double y = candlePos.getY() + 0.5;
            double z = candlePos.getZ() + 0.5;

            float chance = random.nextFloat();
            if (chance < 0.3F) {
                ritual.level.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            ritual.level.sendParticles(ModParticles.RITUAL_FLAME, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // Torches get destroyed during the ritual
    private static boolean isTorch(BlockState state) {
        return state.is(Blocks.TORCH) ||
                state.is(Blocks.WALL_TORCH) ||
                state.is(Blocks.SOUL_TORCH) ||
                state.is(Blocks.SOUL_WALL_TORCH) ||
                // to include or not to include lanterns and other kinds of light sources? Who knows.
                // Ignore soul lanterns since they are naturally spawned in Ancient cities
                state.is(Blocks.LANTERN);
    }

    // Candles do NOT get destroyed but are instead extinguished during the process
    private static boolean isCandle(BlockState state) {
        return state.is(BlockTags.CANDLES) ||
                state.is(BlockTags.CANDLE_CAKES);
    }

    // Custom classes
    private static final class ActiveRitual {
        final ServerLevel level;
        final BlockPos center;
        final BlockPos cityCenter;
        final List<TorchEntry> torches;
        int torchIndex = 0;
        int ticksElapsed = 0;

        ActiveRitual(ServerLevel level, BlockPos center, BlockPos cityCenter, List<TorchEntry> torches) {
            this.level = level;
            this.center = center;
            this.cityCenter = cityCenter;
            this.torches = torches;
        }
    }

    private record TorchEntry(BlockPos pos, double distance) {}
}
