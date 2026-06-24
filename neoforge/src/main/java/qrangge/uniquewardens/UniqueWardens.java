package qrangge.uniquewardens;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import qrangge.uniquewardens.init.UniqueWardensInit;
import qrangge.uniquewardens.registry.ModParticles;
import qrangge.uniquewardens.uniquewardens.NeoForgeUniqueWardensEventHandler;

@Mod(Constants.MOD_ID)
public class UniqueWardens {

    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Constants.MOD_ID);

    private static final DeferredHolder<ParticleType<?>, SimpleParticleType> RITUAL_FLAME =
            PARTICLE_TYPES.register("ritual_flame", () -> new SimpleParticleType(false));

    public UniqueWardens(IEventBus eventBus) {
        Constants.LOG.info("Loading NeoForge uniquewardens...");

        UniqueWardensInit.init();
        NeoForgeUniqueWardensEventHandler.register();

        PARTICLE_TYPES.register(eventBus);
        eventBus.addListener(this::onRegister);
    }

    private void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.PARTICLE_TYPE)) {
            ModParticles.RITUAL_FLAME = RITUAL_FLAME.get();
        }
    }
}