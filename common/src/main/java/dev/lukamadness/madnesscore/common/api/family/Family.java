package dev.lukamadness.madnesscore.common.api.family;

import dev.lukamadness.madnesscore.common.api.species.Species;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.Set;

public record Family(
        ResourceLocation id,
        Component displayName,
        FamilyCompatibility compatibility,
        ResourceLocation group,
        Optional<ResourceLocation> requiredBloodline,
        Set<String> attributes,
        FamilyRequirementReaction onRequirementLost
) {
    public boolean isCompatibleWith(Species species) {
        return compatibility.isCompatibleWith(species);
    }

    public boolean hasAttribute(String attribute) {
        return attributes.contains(attribute);
    }

    public boolean hasGroup() {
        return group != null;
    }
}
