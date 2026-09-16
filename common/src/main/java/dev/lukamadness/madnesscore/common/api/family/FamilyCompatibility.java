package dev.lukamadness.madnesscore.common.api.family;

import dev.lukamadness.madnesscore.common.api.species.Species;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public sealed interface FamilyCompatibility {
    boolean isCompatibleWith(Species species);

    static FamilyCompatibility all() {
        return new All();
    }

    static FamilyCompatibility humanOnly() {
        return new HumanOnly();
    }

    static FamilyCompatibility humanLike() {
        return new HumanLike();
    }

    static FamilyCompatibility specific(ResourceLocation... speciesIds) {
        return new Specific(Set.of(speciesIds));
    }

    static FamilyCompatibility specific(Set<ResourceLocation> speciesIds) {
        return new Specific(Set.copyOf(speciesIds));
    }

    record All() implements FamilyCompatibility {
        @Override
        public boolean isCompatibleWith(Species species) {
            return true;
        }
    }

    record HumanOnly() implements FamilyCompatibility {
        @Override
        public boolean isCompatibleWith(Species species) {
            return species.isHuman();
        }
    }

    record HumanLike() implements FamilyCompatibility {
        @Override
        public boolean isCompatibleWith(Species species) {
            return species.isHumanLike();
        }
    }

    record Specific(Set<ResourceLocation> speciesIds) implements FamilyCompatibility {
        @Override
        public boolean isCompatibleWith(Species species) {
            return speciesIds.contains(species.id());
        }
    }
}
