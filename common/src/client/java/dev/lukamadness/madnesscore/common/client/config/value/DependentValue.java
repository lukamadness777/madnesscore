package dev.lukamadness.madnesscore.common.client.config.value;

import dev.lukamadness.madnesscore.common.client.config.structure.Config;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;

public interface DependentValue<V> {
    V get(Config state);

    default Collection<ResourceLocation> getDependencies() {
        return Set.of();
    }
}
