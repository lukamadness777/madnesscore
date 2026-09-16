package dev.lukamadness.madnesscore.common.impact.material;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A broad classification used to group {@link ImpactMaterial}s (blood, fluid, tissue, bone, oil,
 * energy, etc). Deliberately NOT a Java enum: MadnessCore ships a handful of sensible defaults,
 * but any other mod can register its own category at any time via {@link #register(ResourceLocation)}.
 * <p>
 * A category carries no visual or emission data of its own - it exists purely so that consumers
 * (compat mods, JEI-like tooltips, loot logic, etc.) can reason about "what kind of thing is this"
 * without depending on the id of every single {@link ImpactMaterial}.
 */
public final class ImpactMaterialCategory {
    private static final Map<ResourceLocation, ImpactMaterialCategory> REGISTRY = new LinkedHashMap<>();

    public static final ImpactMaterialCategory BLOOD = register(id("blood"));
    public static final ImpactMaterialCategory FLUID = register(id("fluid"));
    public static final ImpactMaterialCategory TISSUE = register(id("tissue"));
    public static final ImpactMaterialCategory BONE = register(id("bone"));
    public static final ImpactMaterialCategory OIL = register(id("oil"));
    public static final ImpactMaterialCategory ENERGY = register(id("energy"));
    public static final ImpactMaterialCategory CUSTOM = register(id("custom"));

    private final ResourceLocation id;

    private ImpactMaterialCategory(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation id() {
        return id;
    }

    /**
     * Registers a new category. Safe to call from other mods. Registering the same id twice
     * returns the already-registered instance instead of throwing, so init order/duplicate
     * `init()` calls can't crash a game.
     */
    public static synchronized ImpactMaterialCategory register(ResourceLocation id) {
        ImpactMaterialCategory existing = REGISTRY.get(id);
        if (existing != null) {
            return existing;
        }
        ImpactMaterialCategory category = new ImpactMaterialCategory(id);
        REGISTRY.put(id, category);
        return category;
    }

    public static Optional<ImpactMaterialCategory> byId(ResourceLocation id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, path);
    }

    @Override
    public String toString() {
        return "ImpactMaterialCategory[" + id + "]";
    }
}
