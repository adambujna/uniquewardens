package qrangge.uniquewardens.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qrangge.uniquewardens.accessor.WardenAccessor;
import qrangge.uniquewardens.services.Services;
import qrangge.uniquewardens.services.helpers.IDataHelper;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;


@Mixin(SculkShriekerBlockEntity.class)
public class SculkShriekerMixin {
    // Cache city position of this Sculk Shrieker to avoid recalculating city coordinates
    @Unique
    private BlockPos uniquewardens$cachedCityCenter;
    @Unique
    private boolean uniquewardens$checkedForCity = false;

    // Blocks all effects (if city cleared)
    @Inject(method = "tryShriek", at = @At("HEAD"), cancellable = true)
    public void uniquewardens$onTryShriek(ServerLevel level, @Nullable ServerPlayer player, CallbackInfo ci) {

        BlockPos cityCenter = uniquewardens$getOrCacheCityCenter(level);
        if (cityCenter == null) return;

        IDataHelper helper = Services.DATA;

        if (helper.isCityCleared(level, cityCenter)) {
            ci.cancel();
        }
    }

    // Stops duplicate Warden spawns if city not cleared but a Warden is already alive
    // Also failsafe if `uniquewardens$onTryShriek` fails due to potential mod conflict
    // (e.g., trySummonWarden not caused by player)
    @Inject(method = "trySummonWarden", at = @At("HEAD"), cancellable = true)
    private void uniquewardens$onTrySummonWarden (ServerLevel level, CallbackInfoReturnable<Boolean> cir) {
        BlockPos cityCenter = uniquewardens$getOrCacheCityCenter(level);
        if (cityCenter == null) return;

        IDataHelper helper = Services.DATA;

        if (helper.isCityWardenAlive(level, cityCenter) || helper.isCityCleared(level, cityCenter)) {
            cir.setReturnValue(false);
        }
    }

    // If Shrieker spawns Warden, tag it with its home city
    @WrapOperation(
            method = "trySummonWarden",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/SpawnUtil;trySpawnMob(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;IIILnet/minecraft/util/SpawnUtil$Strategy;Z)Ljava/util/Optional;")
    )
    private Optional<Warden> captureWarden(EntityType<Warden> entityType, EntitySpawnReason spawnReason, ServerLevel level, BlockPos start, int spawnAttempts, int spawnRangeXZ, int spawnRangeY, SpawnUtil.Strategy strategy, boolean checkCollisions, Operation<Optional<Warden>> original) {
        // Execute original spawn logic
        Optional<Warden> result = original.call(entityType, spawnReason, level, start, spawnAttempts, spawnRangeXZ, spawnRangeY, strategy, checkCollisions);

        // Extract the Warden if the spawn was successful
        result.ifPresent(warden -> {
            System.out.println("A Shrieker at " + start + " just summoned: " + warden);

            BlockPos cityCenter = uniquewardens$getOrCacheCityCenter(level);
            if (cityCenter != null) {
                // Attach city center to Warden
                ((WardenAccessor) warden).uniquewardens$setHomeCity(cityCenter);

                // Mark the city as having an active Warden
                Services.DATA.markCityWardenAlive(level, cityCenter);
            }
        });

        return result;
    }

    // Gets center position of city which this Shrieker is in either through lookup or cache
    @Unique
    private BlockPos uniquewardens$getOrCacheCityCenter(ServerLevel level) {
        if (uniquewardens$checkedForCity) {
            return uniquewardens$cachedCityCenter;
        }

        SculkShriekerBlockEntity shrieker = (SculkShriekerBlockEntity) (Object) this;
        BlockPos pos = shrieker.getBlockPos();

        var structureRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var ancientCity = structureRegistry.getOrThrow(BuiltinStructures.ANCIENT_CITY);
        var start = level.structureManager().getStructureAt(pos, ancientCity.value());

        if (start.isValid()) {
            this.uniquewardens$cachedCityCenter = start.getBoundingBox().getCenter();
        } else {
            // Fallback: shrieker is "wild" = find the nearest Ancient City within 100 chunks.
            // skipKnownStructures=true means we only search already-generated terrain,
            // so this never forces chunk generation.
            var ancientCityStructure = ancientCity.value();

            var nearest = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, HolderSet.direct(ancientCity), pos, 100, true);

            if (nearest != null) {
                this.uniquewardens$cachedCityCenter =
                        uniquewardens$startCenterFromChunks(level, nearest.getFirst(), ancientCityStructure);
            }
            // If no city found within 100 chunks, cachedCityCenter stays null. Unlucky!
        }

        // Mark that we've checked to skip structure lookup again for this block entity
        this.uniquewardens$checkedForCity = true;
        return uniquewardens$cachedCityCenter;
    }

    // Gets true X, Y, Z position of a found nearest structure (by default Y is 0) for wild shriekers
    @Unique
    private static BlockPos uniquewardens$startCenterFromChunks(
            ServerLevel level, BlockPos nearestPos, Structure structure) {
        ChunkPos origin = new ChunkPos(nearestPos.getX() >> 4, nearestPos.getZ() >> 4);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(origin.x() + dx, origin.z() + dz);
                if (chunk == null) continue;
                StructureStart cityStart = chunk.getAllStarts().get(structure);
                if (cityStart != null && cityStart.isValid()) {
                    return cityStart.getBoundingBox().getCenter();
                }
            }
        }
        return nearestPos; // genuine last resort
    }
}
