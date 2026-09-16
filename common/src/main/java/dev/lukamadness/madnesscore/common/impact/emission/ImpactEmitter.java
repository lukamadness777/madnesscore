package dev.lukamadness.madnesscore.common.impact.emission;

import dev.lukamadness.madnesscore.common.impact.material.ImpactMaterial;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * Knows HOW to turn "this material, at this position" into real particles in the world.
 * <p>
 * Deliberately separate from {@link ImpactMaterial} (which is pure data - see its javadoc): the
 * public API ({@code MadnessCoreImpact.emit(...)}) only ever talks to an {@code ImpactEmitter}, so
 * the underlying rendering/emission mechanism (vanilla particles today, something fancier later -
 * a custom renderer, a GeckoLib effect, whatever) can be swapped out without touching
 * {@link ImpactMaterial} or any mod code that calls the public API.
 */
@FunctionalInterface
public interface ImpactEmitter {
    void emit(ServerLevel level, Vec3 position, ImpactMaterial material, RandomSource random);

    /**
     * The emitter MadnessCore uses unless replaced. Exposed as a constant rather than baked
     * directly into the public API so a compat mod (or MadnessCore itself, later) can swap the
     * underlying emission mechanism at runtime via {@link #set(ImpactEmitter)}.
     */
    ImpactEmitter DEFAULT = new DefaultImpactEmitter();

    static ImpactEmitter current() {
        return Holder.active;
    }

    /**
     * Replaces the emitter used by {@code MadnessCoreImpact.emit(...)}. Intended for advanced
     * integrations only - most mods should just register {@link ImpactMaterial}s instead.
     */
    static void set(ImpactEmitter emitter) {
        Holder.active = emitter;
    }

    /**
     * Holds the currently-active emitter. A separate holder class (rather than a mutable field on
     * the interface itself) is used because interface fields are implicitly {@code public static
     * final} and can't be reassigned.
     */
    final class Holder {
        private static volatile ImpactEmitter active = DEFAULT;

        private Holder() {
        }
    }
}
