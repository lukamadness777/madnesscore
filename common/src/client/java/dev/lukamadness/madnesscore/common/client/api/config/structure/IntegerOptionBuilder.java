package dev.lukamadness.madnesscore.common.client.api.config.structure;

import dev.lukamadness.madnesscore.common.client.api.config.ConfigState;
import dev.lukamadness.madnesscore.common.client.api.config.StorageEventHandler;
import dev.lukamadness.madnesscore.common.client.api.config.option.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IntegerOptionBuilder extends StatefulOptionBuilder<Integer> {
    @Override
    IntegerOptionBuilder setName(Component name);

    @Override
    IntegerOptionBuilder setEnabled(boolean available);

    @Override
    IntegerOptionBuilder setEnabledProvider(Function<ConfigState, Boolean> provider, ResourceLocation... dependencies);

    @Override
    IntegerOptionBuilder setStorageHandler(StorageEventHandler storage);

    @Override
    IntegerOptionBuilder setTooltip(Component tooltip);

    @Override
    IntegerOptionBuilder setTooltip(Function<Integer, Component> tooltip);

    @Override
    IntegerOptionBuilder setImpact(OptionImpact impact);

    @Override
    IntegerOptionBuilder setFlags(OptionFlag... flags);

    @Override
    IntegerOptionBuilder setFlags(ResourceLocation... flags);

    @Override
    IntegerOptionBuilder setDefaultValue(Integer value);

    @Override
    IntegerOptionBuilder setDefaultProvider(Function<ConfigState, Integer> provider, ResourceLocation... dependencies);

    @Override
    IntegerOptionBuilder setControlHiddenWhenDisabled(boolean hidden);

    @Override
    IntegerOptionBuilder setBinding(Consumer<Integer> save, Supplier<Integer> load);

    @Override
    IntegerOptionBuilder setBinding(OptionBinding<Integer> binding);

    @Override
    IntegerOptionBuilder setApplyHook(Consumer<ConfigState> hook);

    IntegerOptionBuilder setRange(int min, int max, int step);

    IntegerOptionBuilder setRange(Range range);

    IntegerOptionBuilder setRangeProvider(Function<ConfigState, ? extends SteppedValidator> provider, ResourceLocation... dependencies);

    IntegerOptionBuilder setValidator(SteppedValidator validator);

    IntegerOptionBuilder setValidatorProvider(Function<ConfigState, ? extends SteppedValidator> provider, ResourceLocation... dependencies);

    IntegerOptionBuilder setValueFormatter(ControlValueFormatter formatter);
}
