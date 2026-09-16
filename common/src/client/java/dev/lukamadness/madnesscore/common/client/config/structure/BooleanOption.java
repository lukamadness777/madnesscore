package dev.lukamadness.madnesscore.common.client.config.structure;

import dev.lukamadness.madnesscore.common.client.api.config.ConfigState;
import dev.lukamadness.madnesscore.common.client.api.config.StorageEventHandler;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionBinding;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionImpact;
import dev.lukamadness.madnesscore.common.client.config.value.DependentValue;
import dev.lukamadness.madnesscore.common.client.gui.options.control.Control;
import dev.lukamadness.madnesscore.common.client.gui.options.control.TickBoxControl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class BooleanOption extends StatefulOption<Boolean> {
    public BooleanOption(
            ResourceLocation id,
            Collection<ResourceLocation> dependencies,
            Component name,
            DependentValue<Boolean> enabled,
            StorageEventHandler storage,
            Function<Boolean, Component> tooltipProvider,
            OptionImpact impact,
            Set<ResourceLocation> flags,
            DependentValue<Boolean> defaultValue,
            Boolean controlHiddenWhenDisabled,
            OptionBinding<Boolean> binding,
            Consumer<ConfigState> applyHook
    ) {
        super(id, dependencies, name, enabled, storage, tooltipProvider, impact, flags, defaultValue, controlHiddenWhenDisabled, binding, applyHook);
    }

    @Override
    Control createControl() {
        return new TickBoxControl(this);
    }

    @Override
    Boolean validateValue(Boolean value) {
        return value;
    }
}
