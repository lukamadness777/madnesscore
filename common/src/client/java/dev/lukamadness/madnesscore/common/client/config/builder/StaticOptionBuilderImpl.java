package dev.lukamadness.madnesscore.common.client.config.builder;

import dev.lukamadness.madnesscore.common.client.api.config.structure.OptionBuilder;
import dev.lukamadness.madnesscore.common.client.config.structure.StaticOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

abstract class StaticOptionBuilderImpl<O extends StaticOption> extends OptionBuilderImpl<O> {
    private Component tooltip;

    StaticOptionBuilderImpl(ResourceLocation id) {
        super(id);
    }

    @Override
    void validateData() {
        Validate.notNull(this.getTooltip(), "Tooltip must be set");
        Validate.notBlank(this.getTooltip().getString(), "Tooltip must not be blank");
    }

    Component getTooltip() {
        return this.getFirstNotNull(this.tooltip, StaticOption::getTooltip);
    }

    @Override
    public OptionBuilder setTooltip(Component tooltip) {
        Validate.notNull(tooltip, "Argument must not be null");

        this.tooltip = tooltip;
        return this;
    }
}
