package qrangge.uniquewardens;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import qrangge.uniquewardens.init.UniqueWardensInit;
import qrangge.uniquewardens.registry.ModParticles;
import qrangge.uniquewardens.uniquewardens.FabricUniqueWardensEventHandler;

public class UniqueWardens implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Constants.LOG.info("Loading Fabric uniquewardens...");
        ModParticles.RITUAL_FLAME = Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "ritual_flame"),
                FabricParticleTypes.simple());

        UniqueWardensInit.init();
        FabricUniqueWardensEventHandler.register();

// // Adds debug command to reset player's shrieker warning level
//        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
//            dispatcher.register(Commands.literal("resetwarnings")
//                    .executes(context -> {
//                        ServerPlayer player = context.getSource().getPlayerOrException();
//
//                        // Access the vanilla warden spawn tracker on the player
//                        player.getWardenSpawnTracker().ifPresent(WardenSpawnTracker::reset);
//
//                        context.getSource().sendSuccess(() -> Component.literal("Warden warning levels reset to 0!"), false);
//                        return 1;
//                    })
//            );
//        });
    }
}
