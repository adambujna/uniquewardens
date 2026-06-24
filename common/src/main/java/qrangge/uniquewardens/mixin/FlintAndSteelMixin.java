package qrangge.uniquewardens.mixin;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qrangge.uniquewardens.ritual.RitualHandler;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelMixin {

    // Checks whether to summon a Warden spawning ritual whenever a candle is lit using Flint and Steel.
    @Inject(method = "useOn", at = @At("RETURN"))
    private void uniquewardens$onLightCandle(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        var pos = context.getClickedPos();
        var state = serverLevel.getBlockState(pos);

        if (state.is(Blocks.CANDLE) && state.getValue(BlockStateProperties.LIT)) {
            RitualHandler.onCandleLit(serverLevel, pos);
        }
    }
}
