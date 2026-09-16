package dev.lukamadness.madnesscore.common.api.bloodline;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Set;

public final class BloodlineBuilder {
    private final ResourceLocation id;
    private Component displayName;
    private BloodlineCompatibility compatibility = BloodlineCompatibility.all();
    private final Set<String> attributes = new LinkedHashSet<>();
    private boolean natural = false;
    private boolean obtainable = true;
    private boolean heritable = false;
    private boolean exclusiveObtainMethod = false;

    private BloodlineBuilder(ResourceLocation id) {
        this.id = id;
        this.displayName = Component.translatable("bloodline." + id.getNamespace() + "." + id.getPath());
    }

    public static BloodlineBuilder create(ResourceLocation id) {
        return new BloodlineBuilder(id);
    }

    public BloodlineBuilder displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public BloodlineBuilder compatibility(BloodlineCompatibility compatibility) {
        this.compatibility = compatibility;
        return this;
    }

    public BloodlineBuilder attribute(String attribute) {
        this.attributes.add(attribute);
        return this;
    }

    public BloodlineBuilder attributes(String... attributes) {
        this.attributes.addAll(Set.of(attributes));
        return this;
    }

    public BloodlineBuilder natural(boolean natural) {
        this.natural = natural;
        return this;
    }

    public BloodlineBuilder obtainable(boolean obtainable) {
        this.obtainable = obtainable;
        return this;
    }

    public BloodlineBuilder heritable(boolean heritable) {
        this.heritable = heritable;
        return this;
    }

    public BloodlineBuilder exclusiveObtainMethod(boolean exclusiveObtainMethod) {
        this.exclusiveObtainMethod = exclusiveObtainMethod;
        return this;
    }

    public Bloodline register() {
        return BloodlineRegistry.register(id, displayName, compatibility, Set.copyOf(attributes),
                natural, obtainable, heritable, exclusiveObtainMethod);
    }
}
