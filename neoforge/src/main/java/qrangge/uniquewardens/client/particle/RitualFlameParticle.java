package qrangge.uniquewardens.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class RitualFlameParticle {
    private RitualFlameParticle() {}

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType options, ClientLevel level, double x, double y, double z,
                double xAux, double yAux, double zAux) {
            FlameParticle particle = new FlameParticle(level, x, y, z, xAux, yAux, zAux);
            particle.pickSprite(this.sprite);
            particle.scale(0.5F);
            return particle;
        }
    }
}
