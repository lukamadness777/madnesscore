package dev.lukamadness.madnesscore.common.api.bloodline;

import dev.lukamadness.madnesscore.common.api.species.Species;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public sealed interface BloodlineCompatibility {
    boolean isCompatibleWith(Species species);

    static BloodlineCompatibility all() {
        return new All();
    }

    static BloodlineCompatibility humanOnly() {
        return new HumanOnly();
    }

    static BloodlineCompatibility humanLike() {
        return new HumanLike();
    }

    static BloodlineCompatibility specific(ResourceLocation... speciesIds) {
        return new Specific(Set.of(speciesIds));
    }

    static BloodlineCompatibility specific(Set<ResourceLocation> speciesIds) {
        return new Specific(Set.copyOf(speciesIds));
    }

    record All() implements BloodlineCompatibility {
        @Override
        public boolean isCompatibleWith(Species species) {
            return true;
        }
    }

    record HumanOnly() implements BloodlineCompatibility {
        @Override
        public boolean isCompatibleWith(Species species) {
            return species.isHuman();
        }
    }

    record HumanLike() implements BloodlineCompatibility {
        @Override
        public boolean isCompatibleWith(Species species) {
            return species.isHumanLike();
        }
    }

    record Specific(Set<ResourceLocation> speciesIds) implements BloodlineCompatibility {
        @Override
        public boolean isCompatibleWith(Species species) {
            return speciesIds.contains(species.id());
        }
    }
}
