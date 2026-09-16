package dev.lukamadness.madnesscore.common.api.species;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record Species(ResourceLocation id, Component displayName, boolean isHumanLike, double weight) {
    public boolean isHuman() {
        return id.equals(SpeciesRegistry.HUMAN_ID);
    }
}
