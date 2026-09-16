package dev.lukamadness.madnesscore.common.impact.material;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;

/**
 * Produces the {@link ParticleOptions} to use for a single emitted particle.
 * <p>
 * Kept as a functional interface (rather than a fixed field on {@link ImpactMaterial}) so a
 * material can vary its appearance per-particle - e.g. a slightly randomized dust color, or a
 * choice between a couple of block/item textures - without MadnessCore needing to know anything
 * about the specific vanilla or modded particle type being used underneath.
 */
@FunctionalInterface
public interface ImpactParticleFactory {
    ParticleOptions create(RandomSource random);

    /**
     * Convenience factory for materials that always use the exact same {@link ParticleOptions}.
     */
    static ImpactParticleFactory constant(ParticleOptions options) {
        return random -> options;
    }
}
