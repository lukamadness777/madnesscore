package dev.lukamadness.madnesscore.common.impact.material;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Public, extensible registry of {@link ImpactMaterial}s.
 * <p>
 * Any mod can register its own materials here (no need for a MadnessCore-specific dependency on
 * "blood" - the API doesn't know or care what kind of material it's handling), and can optionally
 * tell MadnessCore which material a given entity uses, so calling code only has to say
 * "this entity was just impacted" and let MadnessCore work out the rest (see the resolution
 * helpers below and {@code MadnessCoreImpact}).
 */
public final class ImpactMaterialRegistry {
    private static final Map<ResourceLocation, ImpactMaterial> MATERIALS = new LinkedHashMap<>();
    private static final Map<EntityType<?>, ImpactMaterial> BY_ENTITY_TYPE = new LinkedHashMap<>();
    private static final List<Function<Entity, Optional<ImpactMaterial>>> RESOLVERS = new ArrayList<>();

    private ImpactMaterialRegistry() {
    }

    /**
     * Registers a material, making it available by id and to the {@code /madnesscore} data-driven
     * tools (JEI-style listings, debug commands, etc). Re-registering the same id replaces the
     * previous definition, which is intentional: it lets a datapack/config layer or a later-loaded
     * compat mod override a built-in material such as {@code madnesscore:blood}.
     */
    public static synchronized ImpactMaterial register(ImpactMaterial material) {
        MATERIALS.put(material.id(), material);
        return material;
    }

    public static synchronized Optional<ImpactMaterial> get(ResourceLocation id) {
        return Optional.ofNullable(MATERIALS.get(id));
    }

    public static synchronized Collection<ImpactMaterial> all() {
        return List.copyOf(MATERIALS.values());
    }

    /**
     * Declares that a given entity type always expels {@code material} when impacted. This is the
     * simplest way for a mod to hook its entities into the generic system, e.g.
     * {@code ImpactMaterialRegistry.bindEntityType(EntityType.ENDERMAN, ImpactMaterials.FLUID)}.
     */
    public static synchronized void bindEntityType(EntityType<?> entityType, ImpactMaterial material) {
        BY_ENTITY_TYPE.put(entityType, material);
    }

    /**
     * Registers a resolver for cases entity-type binding can't express (e.g. picking a material
     * based on NBT, a capability, a tag, or entity-specific state). Resolvers are tried in
     * registration order, before the entity-type bindings and the built-in fallback, so a mod can
     * override MadnessCore's own defaults if it needs to.
     */
    public static synchronized void addResolver(Function<Entity, Optional<ImpactMaterial>> resolver) {
        RESOLVERS.add(resolver);
    }

    /**
     * Works out which material {@code entity} should expel on impact, in order:
     * <ol>
     *     <li>registered {@link #addResolver resolvers}, in registration order</li>
     *     <li>a direct {@link #bindEntityType entity-type binding}</li>
     *     <li>a built-in fallback: {@code ImpactMaterials.BLOOD} for any {@link LivingEntity}</li>
     * </ol>
     * Returns empty if none of the above apply, meaning no impact effect should be produced.
     */
    public static synchronized Optional<ImpactMaterial> resolve(Entity entity) {
        for (Function<Entity, Optional<ImpactMaterial>> resolver : RESOLVERS) {
            Optional<ImpactMaterial> resolved = resolver.apply(entity);
            if (resolved.isPresent()) {
                return resolved;
            }
        }

        ImpactMaterial bound = BY_ENTITY_TYPE.get(entity.getType());
        if (bound != null) {
            return Optional.of(bound);
        }

        if (entity instanceof LivingEntity) {
            return get(ImpactMaterials.BLOOD.id());
        }

        return Optional.empty();
    }

    /**
     * Test-only / reload-safe reset. Not used by normal mod init - exposed for datapack reload
     * hooks or unit tests that need a clean slate.
     */
    public static synchronized void clear() {
        MATERIALS.clear();
        BY_ENTITY_TYPE.clear();
        RESOLVERS.clear();
        MadnessCoreCommon.LOG.debug("[impact] material registry cleared");
    }
}
