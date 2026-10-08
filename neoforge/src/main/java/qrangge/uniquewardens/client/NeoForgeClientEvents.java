package qrangge.uniquewardens.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import qrangge.uniquewardens.Constants;
import qrangge.uniquewardens.client.particle.RitualFlameParticle;
import qrangge.uniquewardens.registry.ModParticles;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NeoForgeClientEvents {
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.RITUAL_FLAME, RitualFlameParticle.Provider::new);
    }
}
