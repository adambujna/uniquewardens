package qrangge.uniquewardens.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.monster.warden.Warden;
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
    private void uniquewardens$saveHomeCity(CompoundTag tag, CallbackInfo ci) {
        if (this.uniquewardens$homeCityCenter != null) {
            BlockPos p = this.uniquewardens$homeCityCenter;
            tag.putIntArray("uniquewardens:home_city", new int[]{p.getX(), p.getY(), p.getZ()});
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void uniquewardens$loadHomeCity(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("uniquewardens:home_city", Tag.TAG_INT_ARRAY)) {
            int[] a = tag.getIntArray("uniquewardens:home_city");
            if (a.length == 3) {
                this.uniquewardens$homeCityCenter = new BlockPos(a[0], a[1], a[2]);
            }
        }
    }
}