package dev.lukamadness.madnesscore.common.api.family;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class FamilyBuilder {
    private final ResourceLocation id;
    private Component displayName;
    private FamilyCompatibility compatibility = FamilyCompatibility.all();
    private ResourceLocation group;
    private ResourceLocation requiredBloodline;
    private final Set<String> attributes = new LinkedHashSet<>();
    private FamilyRequirementReaction onRequirementLost = FamilyRequirementReaction.REMOVE;

    private FamilyBuilder(ResourceLocation id) {
        this.id = id;
        this.displayName = Component.translatable("family." + id.getNamespace() + "." + id.getPath());
    }

    public static FamilyBuilder create(ResourceLocation id) {
        return new FamilyBuilder(id);
    }

    public FamilyBuilder displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public FamilyBuilder compatibility(FamilyCompatibility compatibility) {
        this.compatibility = compatibility;
        return this;
    }

    public FamilyBuilder group(ResourceLocation group) {
        this.group = group;
        return this;
    }

    public FamilyBuilder requiredBloodline(ResourceLocation requiredBloodline) {
        this.requiredBloodline = requiredBloodline;
        return this;
    }

    public FamilyBuilder attribute(String attribute) {
        this.attributes.add(attribute);
        return this;
    }

    public FamilyBuilder attributes(String... attributes) {
        this.attributes.addAll(Set.of(attributes));
        return this;
    }

    public FamilyBuilder onRequirementLost(FamilyRequirementReaction onRequirementLost) {
        this.onRequirementLost = onRequirementLost;
        return this;
    }

    public Family register() {
        return FamilyRegistry.register(id, displayName, compatibility, group,
                Optional.ofNullable(requiredBloodline), Set.copyOf(attributes), onRequirementLost);
    }
}
