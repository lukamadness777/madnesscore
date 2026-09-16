package dev.lukamadness.madnesscore.common.impact;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.impact.emission.ImpactEmitter;
import dev.lukamadness.madnesscore.common.impact.material.ImpactMaterial;
import dev.lukamadness.madnesscore.common.impact.material.ImpactMaterialRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Public MadnessCore API for the generic impact-material system (section 2+ of the design doc):
 * any mod can say "this entity just got hit" and let MadnessCore work out what comes out and how,
 * without copy-pasting its own particle-loop code.
 * <p>
 * This is intentionally NOT called {@code BloodAPI}: it is not specific to blood, or even to
 * particles - the material system in {@code impact.material} already covers fluids, tissue, bone
 * dust, oil, energy and any custom material another mod registers, and the actual emission
 * mechanism ({@link ImpactEmitter}) can be swapped without changing this class's signatures.
 * <p>
 * Emission is server-authoritative: calls are no-ops unless {@code entity.level()} is a
 * {@link ServerLevel}, matching how damage/impacts are normally resolved in vanilla. This avoids
 * needing any custom client/server networking for the base system - see {@code DefaultImpactEmitter}.
 */
public final class MadnessCoreImpact {
    private MadnessCoreImpact() {
    }

    /**
     * Emits {@code material} from {@code position}, attributed to {@code entity} (used only for
     * its level/random source - the material is exactly what's passed in). This is the fully
     * explicit form: use it when the calling mod already knows exactly what should come out, e.g.
     * a modded entity that always bleeds oil.
     */
    public static void emit(Entity entity, Vec3 position, ImpactMaterial material) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ImpactEmitter.current().emit(serverLevel, position, material, entity.getRandom());
    }

    /**
     * Emits {@code material} from the entity's own position (approximated as the center of its
     * bounding box). Convenience for callers that don't have a precise impact point handy.
     */
    public static void emit(Entity entity, ImpactMaterial material) {
        emit(entity, impactPoint(entity), material);
    }

    /**
     * "This entity just got hit at this position" - MadnessCore determines the material via
     * {@link ImpactMaterialRegistry#resolve(Entity)} (entity-type bindings, custom resolvers, or
     * the built-in living-entity fallback). Does nothing if no material could be resolved.
     */
    public static void emit(Entity entity, Vec3 position) {
        Optional<ImpactMaterial> material = ImpactMaterialRegistry.resolve(entity);
        if (material.isEmpty()) {
            MadnessCoreCommon.LOG.debug("[impact] no material resolved for {}, skipping impact effect",
                    entity.getType());
            return;
        }
        emit(entity, position, material.get());
    }

    /**
     * "This entity just got hit" - fully automatic: MadnessCore determines both the impact point
     * and the material. The simplest possible integration for another mod.
     */
    public static void emit(Entity entity) {
        emit(entity, impactPoint(entity));
    }

    private static Vec3 impactPoint(Entity entity) {
        return entity.getBoundingBox().getCenter();
    }
}
