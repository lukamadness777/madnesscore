package dev.lukamadness.madnesscore.common.client.config.structure;

import dev.lukamadness.madnesscore.common.client.config.value.DependentValue;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public abstract class StaticOption extends Option {
    final Component tooltip;

    StaticOption(
            ResourceLocation id,
            Collection<ResourceLocation> dependencies,
            Component name, DependentValue<Boolean> enabled,
            Component tooltip
    ) {
        super(id, dependencies, name, enabled);
        this.tooltip = tooltip;
    }

    @Override
    public Component getTooltip() {
        return this.tooltip;
    }
}
