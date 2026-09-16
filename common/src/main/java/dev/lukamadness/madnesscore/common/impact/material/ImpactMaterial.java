package dev.lukamadness.madnesscore.common.impact.material;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Describes WHAT gets expelled by an entity when it is impacted - blood, fluid, tissue, bone dust,
 * oil, energy, or any custom material a mod wants to register. This is pure data: it says nothing
 * about how particles actually get spawned into the world (see {@code ImpactEmitter}), which keeps
 * rendering/emission free to change later without breaking this API.
 * <p>
 * Not a fixed set of hardcoded types: any mod can build and register its own {@code ImpactMaterial}
 * via {@link ImpactMaterialRegistry#register(ImpactMaterial)}.
 *
 * @param id              unique identifier, e.g. {@code madnesscore:blood} or {@code mymod:hydraulic_fluid}
 * @param category         broad classification used for grouping/compat, see {@link ImpactMaterialCategory}
 * @param displayColor     an approximate ARGB color for this material, useful for tooltips/compat/map dots -
 *                         NOT the mechanism used to render particles, just descriptive metadata
 * @param particleFactory  decides the actual {@link net.minecraft.core.particles.ParticleOptions} used per particle
 * @param emission         tuning for count/spread/speed of the emission, see {@link ImpactEmissionProfile}
 */
public record ImpactMaterial(
        ResourceLocation id,
        ImpactMaterialCategory category,
        int displayColor,
        ImpactParticleFactory particleFactory,
        ImpactEmissionProfile emission
) {
    public ImpactMaterial {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(particleFactory, "particleFactory");
        Objects.requireNonNull(emission, "emission");
    }

    /**
     * Returns a copy of this material with a different {@link ImpactEmissionProfile}, letting a
     * mod reuse another material's appearance (color/particle) while tuning how violently it's
     * expelled - e.g. a "weak" vs "critical" hit variant of the same blood.
     */
    public ImpactMaterial withEmission(ImpactEmissionProfile emission) {
        return new ImpactMaterial(id, category, displayColor, particleFactory, emission);
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private ImpactMaterialCategory category = ImpactMaterialCategory.CUSTOM;
        private int displayColor = 0xFFFFFFFF;
        private ImpactParticleFactory particleFactory;
        private ImpactEmissionProfile emission = ImpactEmissionProfile.DEFAULT;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder category(ImpactMaterialCategory category) {
            this.category = category;
            return this;
        }

        public Builder displayColor(int argb) {
            this.displayColor = argb;
            return this;
        }

        public Builder particle(ImpactParticleFactory particleFactory) {
            this.particleFactory = particleFactory;
            return this;
        }

        public Builder emission(ImpactEmissionProfile emission) {
            this.emission = emission;
            return this;
        }

        public ImpactMaterial build() {
            if (particleFactory == null) {
                throw new IllegalStateException("ImpactMaterial '" + id + "' has no particle factory");
            }
            return new ImpactMaterial(id, category, displayColor, particleFactory, emission);
        }
    }
}
