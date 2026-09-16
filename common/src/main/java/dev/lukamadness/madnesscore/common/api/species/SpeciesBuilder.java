package dev.lukamadness.madnesscore.common.api.species;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class SpeciesBuilder {
    private final ResourceLocation id;
    private Component displayName;
    private boolean isHumanLike = true;
    private double weight = 1.0;

    private SpeciesBuilder(ResourceLocation id) {
        this.id = id;
        this.displayName = Component.translatable("species." + id.getNamespace() + "." + id.getPath());
    }

    public static SpeciesBuilder create(ResourceLocation id) {
        return new SpeciesBuilder(id);
    }

    public SpeciesBuilder displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public SpeciesBuilder humanLike(boolean humanLike) {
        this.isHumanLike = humanLike;
        return this;
    }

    public SpeciesBuilder weight(double weight) {
        this.weight = weight;
        return this;
    }

    public Species register() {
        return SpeciesRegistry.register(id, displayName, isHumanLike, weight);
    }
}
