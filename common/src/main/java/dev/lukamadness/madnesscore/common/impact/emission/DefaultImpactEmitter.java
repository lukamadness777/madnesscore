package dev.lukamadness.madnesscore.common.impact.emission;

import dev.lukamadness.madnesscore.common.impact.material.ImpactEmissionProfile;
import dev.lukamadness.madnesscore.common.impact.material.ImpactMaterial;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * Generalizes the behaviour Beyond The Sea's {@code spawnBloodParticles} used as a reference: a
 * handful of particles thrown out from the impact point with randomized position/velocity and a
 * slight upward bias.
 * <p>
 * Unlike that reference (which spawned particles purely client-side, in response to a mod-specific
 * network payload), this emitter is server-authoritative: it uses vanilla's own
 * {@link ServerLevel#sendParticles} to broadcast one {@code ClientboundLevelParticlesPacket} per
 * particle to every client already tracking the area, with an explicit position and velocity
 * (the {@code count == 0} form of that call, which vanilla itself uses for e.g. critical-hit
 * particles). That means:
 * <ul>
 *     <li>no custom networking is required - MadnessCore doesn't need its own payload/codec for this</li>
 *     <li>rendering is handled entirely by the client's existing, vanilla {@code ParticleEngine} -
 *     any {@link ParticleOptions} MadnessCore or another mod supplies (vanilla or custom-registered)
 *     "just works" without MadnessCore needing loader-specific client code for the base case</li>
 * </ul>
 */
public final class DefaultImpactEmitter implements ImpactEmitter {
    @Override
    public void emit(ServerLevel level, Vec3 position, ImpactMaterial material, RandomSource random) {
        ImpactEmissionProfile emission = material.emission();
        int count = emission.rollCount(random);

        for (int i = 0; i < count; i++) {
            ParticleOptions particle = material.particleFactory().create(random);

            double px = position.x() + emission.rollOffset(random);
            double py = position.y() + emission.rollOffset(random);
            double pz = position.z() + emission.rollOffset(random);

            double vx = emission.rollHorizontalVelocity(random);
            double vy = emission.rollVerticalVelocity(random);
            double vz = emission.rollHorizontalVelocity(random);

            // count = 0 tells the client to treat (vx, vy, vz) as an explicit per-particle
            // velocity instead of a random-direction jitter range, which is what lets us
            // reproduce the reference's per-particle randomized offset + upward-biased velocity.
            level.sendParticles(particle, px, py, pz, 0, vx, vy, vz, 1.0D);
        }
    }
}
