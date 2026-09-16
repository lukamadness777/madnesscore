package dev.lukamadness.madnesscore.common.client.config.structure;

import dev.lukamadness.madnesscore.common.client.config.builder.OptionBuilderImpl;
import net.minecraft.resources.ResourceLocation;

public record OptionOverlay(ResourceLocation target, String source, OptionBuilderImpl<?> change, int priority) {
    public OptionOverlay(ResourceLocation target, String source, OptionBuilderImpl<?> change) {
        this(target, source, change, 0);
    }
}
