package dev.lukamadness.madnesscore.common.impact.material;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Purely numeric tuning for HOW a material is thrown out on impact: how many particles, how far
 * they scatter from the impact point, and how fast they fly off.
 * <p>
 * This is intentionally a separate record from {@link ImpactMaterial} (see the class javadoc
 * there): a material's identity/appearance and its emission physics can be tuned independently,
 * and this profile carries no knowledge of *how* particles actually get spawned into the world -
 * that mechanism lives in {@code ImpactEmitter}.
 *
 * @param minCount          minimum number of particles spawned per impact (inclusive)
 * @param maxCount          maximum number of particles spawned per impact (inclusive)
 * @param spread            max per-axis positional jitter around the impact point, in blocks
 * @param horizontalSpeed   max per-axis horizontal velocity jitter (symmetric around 0)
 * @param minVerticalSpeed  minimum vertical velocity applied to each particle
 * @param maxVerticalSpeed  maximum vertical velocity applied to each particle
 */
public record ImpactEmissionProfile(
        int minCount,
        int maxCount,
        float spread,
        float horizontalSpeed,
        float minVerticalSpeed,
        float maxVerticalSpeed
) {
    /**
     * Mirrors the reference Beyond The Sea behaviour this system generalizes: 10 particles,
     * +-0.2 positional jitter, +-0.15 horizontal velocity jitter, 0..0.3 upward velocity bias.
     */
    public static final ImpactEmissionProfile DEFAULT =
            new ImpactEmissionProfile(10, 10, 0.2F, 0.15F, 0.0F, 0.3F);

    public ImpactEmissionProfile {
        if (minCount < 0 || maxCount < minCount) {
            throw new IllegalArgumentException("Invalid particle count range [" + minCount + ", " + maxCount + "]");
        }
        if (maxVerticalSpeed < minVerticalSpeed) {
            throw new IllegalArgumentException("maxVerticalSpeed must be >= minVerticalSpeed");
        }
    }

    public int rollCount(RandomSource random) {
        return minCount == maxCount ? minCount : Mth.nextInt(random, minCount, maxCount);
    }

    public double rollOffset(RandomSource random) {
        return (random.nextDouble() - 0.5D) * spread;
    }

    public double rollHorizontalVelocity(RandomSource random) {
        return (random.nextDouble() - 0.5D) * horizontalSpeed;
    }

    public double rollVerticalVelocity(RandomSource random) {
        return minVerticalSpeed + random.nextDouble() * (maxVerticalSpeed - minVerticalSpeed);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int minCount = 10;
        private int maxCount = 10;
        private float spread = 0.2F;
        private float horizontalSpeed = 0.15F;
        private float minVerticalSpeed = 0.0F;
        private float maxVerticalSpeed = 0.3F;

        public Builder count(int exact) {
            this.minCount = exact;
            this.maxCount = exact;
            return this;
        }

        public Builder count(int min, int max) {
            this.minCount = min;
            this.maxCount = max;
            return this;
        }

        public Builder spread(float spread) {
            this.spread = spread;
            return this;
        }

        public Builder horizontalSpeed(float horizontalSpeed) {
            this.horizontalSpeed = horizontalSpeed;
            return this;
        }

        public Builder verticalSpeed(float min, float max) {
            this.minVerticalSpeed = min;
            this.maxVerticalSpeed = max;
            return this;
        }

        public ImpactEmissionProfile build() {
            return new ImpactEmissionProfile(minCount, maxCount, spread, horizontalSpeed, minVerticalSpeed, maxVerticalSpeed);
        }
    }
}
