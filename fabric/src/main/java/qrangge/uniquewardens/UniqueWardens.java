package qrangge.uniquewardens;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import qrangge.uniquewardens.init.UniqueWardensInit;
import qrangge.uniquewardens.registry.ModParticles;
import qrangge.uniquewardens.uniquewardens.FabricUniqueWardensEventHandler;

public class UniqueWardens implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Constants.LOG.info("Loading Fabric uniquewardens...");
        ModParticles.RITUAL_FLAME = Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "ritual_flame"),
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
