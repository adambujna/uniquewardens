package qrangge.uniquewardens.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import qrangge.uniquewardens.client.particle.RitualFlameParticle;
import qrangge.uniquewardens.registry.ModParticles;

public class UniqueWardensClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance()
                .register(ModParticles.RITUAL_FLAME, RitualFlameParticle.Provider::new);
    }
}
