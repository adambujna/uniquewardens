package qrangge.uniquewardens.mixin;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qrangge.uniquewardens.accessor.WardenAccessor;


@Mixin(Warden.class)
public class WardenMixin implements WardenAccessor {
    @Unique
    private BlockPos uniquewardens$homeCityCenter;

    public void uniquewardens$setHomeCity(BlockPos pos) {
        this.uniquewardens$homeCityCenter = pos;
    }

    public BlockPos uniquewardens$getHomeCity() {
        return this.uniquewardens$homeCityCenter;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void uniquewardens$saveHomeCity(ValueOutput output, CallbackInfo ci) {
        if (this.uniquewardens$homeCityCenter != null) {
            output.store("uniquewardens:home_city", BlockPos.CODEC, this.uniquewardens$homeCityCenter);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void uniquewardens$loadHomeCity(ValueInput input, CallbackInfo ci) {
        input.read("uniquewardens:home_city", BlockPos.CODEC)
                .ifPresent(pos -> this.uniquewardens$homeCityCenter = pos);
    }
}

