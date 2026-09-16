package dev.lukamadness.madnesscore.common.api.bloodline;

import dev.lukamadness.madnesscore.common.api.species.Species;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public record Bloodline(
        ResourceLocation id,
        Component displayName,
        BloodlineCompatibility compatibility,
        Set<String> attributes,
        boolean natural,
        boolean obtainable,
        boolean heritable,
        boolean exclusiveObtainMethod
) {
    public boolean isCompatibleWith(Species species) {
        return compatibility.isCompatibleWith(species);
    }

    public boolean hasAttribute(String attribute) {
        return attributes.contains(attribute);
    }
}
