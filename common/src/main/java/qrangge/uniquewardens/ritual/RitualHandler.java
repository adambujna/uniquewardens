package qrangge.uniquewardens.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import qrangge.uniquewardens.services.Services;
import qrangge.uniquewardens.util.AncientCityLookup;

// Checks if a candle was lit on soul sand and if the soul sand is part of a valid spawn configuration.
// Valid spawn configuration: Redstone middle with 4 soulsand around it in a cross with lit candles on top.
public class RitualHandler {
    private RitualHandler() {}

    // Relative positions of soul sand to the center of the portal.
    // Package-visible so RitualProgressTracker can reuse the same offsets on completion.
    static final BlockPos[] CROSS = {
            new BlockPos(1, 0, 0), // >
            new BlockPos(-1, 0, 0), //
            new BlockPos(0, 0, 1), // ^
            new BlockPos(0, 0, -1) // v
    };

    // Called whenever a candle gets lit (see FlintAndSteelMixin)
    public static void onCandleLit(ServerLevel level, BlockPos candlePos) {
        BlockPos soulSandPos = candlePos.below();
        if (!level.getBlockState(soulSandPos).is(Blocks.SOUL_SAND)) return;

        for (BlockPos offset : CROSS) {
            BlockPos candidateCenter = soulSandPos.subtract(offset);
            if (tryTriggerRitual(level, candidateCenter)) return;
        }
    }

    private static boolean tryTriggerRitual(ServerLevel level, BlockPos center) {
        if (!level.getBlockState(center).is(Blocks.REDSTONE_BLOCK)) return false;

        for (BlockPos offset : CROSS) {
            BlockPos sandPos = center.offset(offset);
            if (!level.getBlockState(sandPos).is(Blocks.SOUL_SAND)) return false;

            BlockState candle = level.getBlockState(sandPos.above());
            if (!candle.is(Blocks.CANDLE)
                    || !candle.getValue(BlockStateProperties.LIT)
                    || candle.getValue(BlockStateProperties.CANDLES) != 1) {
                return false;
            }
        }

        BlockPos cityCenter = AncientCityLookup.findCityCenter(level, center);
        if (cityCenter == null || !Services.DATA.isCityCleared(level, cityCenter)) return false;

        triggerRitual(level, center, cityCenter);
        return true;
    }

    private static void triggerRitual(ServerLevel level, BlockPos center, BlockPos cityCenter) {
        // Audio cue the ritual has begun.
        level.playSound(null, center, SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.BLOCKS, 2.0f, 0.7f);
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, new AABB(center).inflate(40))) {
            p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60,
                    0, false, false));
        }

        // Extinguish the 4 ignition candles — RitualSequence takes over their visual
        // fire from here using hand-spawned soul-fire particles instead of the model flame.
        for (BlockPos offset : CROSS) {
            BlockPos candlePos = center.offset(offset).above();
            BlockState candle = level.getBlockState(candlePos);
            if (candle.is(Blocks.CANDLE)
                    && candle.getValue(BlockStateProperties.LIT)
                    && candle.getValue(BlockStateProperties.CANDLES) == 1) {
                level.setBlock(candlePos, candle.setValue(BlockStateProperties.LIT, false), 3);
                // Add artificial light
                BlockPos lightPos = candlePos.above();
                level.setBlock(lightPos, Blocks.LIGHT.defaultBlockState()
                        .setValue(BlockStateProperties.LEVEL, 4), 3);
            }
        }


        RitualSequence.start(level, center, cityCenter);
    }
}