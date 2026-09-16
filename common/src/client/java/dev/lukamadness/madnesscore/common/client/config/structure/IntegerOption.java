package dev.lukamadness.madnesscore.common.client.config.structure;

import dev.lukamadness.madnesscore.common.client.api.config.ConfigState;
import dev.lukamadness.madnesscore.common.client.api.config.StorageEventHandler;
import dev.lukamadness.madnesscore.common.client.api.config.option.ControlValueFormatter;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionBinding;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionImpact;
import dev.lukamadness.madnesscore.common.client.api.config.option.SteppedValidator;
import dev.lukamadness.madnesscore.common.client.config.value.DependentValue;
import dev.lukamadness.madnesscore.common.client.gui.options.control.Control;
import dev.lukamadness.madnesscore.common.client.gui.options.control.SliderControl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class IntegerOption extends StatefulOption<Integer> {
    private final DependentValue<? extends SteppedValidator> validator;
    private final ControlValueFormatter valueFormatter;

    public IntegerOption(
            ResourceLocation id,
            Collection<ResourceLocation> dependencies,
            Component name,
            DependentValue<Boolean> enabled,
            StorageEventHandler storage,
            Function<Integer, Component> tooltipProvider,
            OptionImpact impact,
            Set<ResourceLocation> flags,
            DependentValue<Integer> defaultValue,
            Boolean controlHiddenWhenDisabled,
            OptionBinding<Integer> binding,
            Consumer<ConfigState> applyHook,
            DependentValue<? extends SteppedValidator> validator,
            ControlValueFormatter valueFormatter
    ) {
        super(id, dependencies, name, enabled, storage, tooltipProvider, impact, flags, defaultValue, controlHiddenWhenDisabled, binding, applyHook);
        this.validator = validator;
        this.valueFormatter = valueFormatter;
    }

    @Override
    void visitDependentValues(Consumer<DependentValue<?>> visitor) {
        super.visitDependentValues(visitor);
        visitor.accept(this.validator);
    }

    @Override
    Integer validateValue(Integer value) {
        if (this.validator != null) {
            return this.validator.get(this.state).getValidatedValue(value, () -> this.defaultValue.get(this.state));
        } else {
            return value;
        }
    }

    @Override
    Control createControl() {
        return new SliderControl(this);
    }

    public SteppedValidator getSteppedValidator() {
        return this.validator.get(this.state);
    }

    public Component formatValue(int value) {
        return this.valueFormatter.format(value);
    }

    public DependentValue<? extends SteppedValidator> getValidatorProvider() {
        return this.validator;
    }

    public ControlValueFormatter getValueFormatter() {
        return this.valueFormatter;
    }
}
