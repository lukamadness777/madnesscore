package dev.lukamadness.madnesscore.common.client.api.config.structure;

import dev.lukamadness.madnesscore.common.client.api.config.ConfigState;
import dev.lukamadness.madnesscore.common.client.api.config.StorageEventHandler;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionBinding;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionFlag;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionImpact;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface StatefulOptionBuilder<V> extends OptionBuilder {
    @Override
    StatefulOptionBuilder<V> setName(Component name);

    @Override
    OptionBuilder setEnabled(boolean available);

    @Override
    OptionBuilder setEnabledProvider(Function<ConfigState, Boolean> provider, ResourceLocation... dependencies);

    StatefulOptionBuilder<V> setStorageHandler(StorageEventHandler storage);

    @Override
    StatefulOptionBuilder<V> setTooltip(Component tooltip);

    StatefulOptionBuilder<V> setTooltip(Function<V, Component> tooltip);

    StatefulOptionBuilder<V> setImpact(OptionImpact impact);

    StatefulOptionBuilder<V> setFlags(OptionFlag... flags);

    StatefulOptionBuilder<V> setFlags(ResourceLocation... flags);

    StatefulOptionBuilder<V> setDefaultValue(V value);

    StatefulOptionBuilder<V> setDefaultProvider(Function<ConfigState, V> provider, ResourceLocation... dependencies);

    StatefulOptionBuilder<V> setControlHiddenWhenDisabled(boolean hidden);

    StatefulOptionBuilder<V> setBinding(Consumer<V> save, Supplier<V> load);

    StatefulOptionBuilder<V> setBinding(OptionBinding<V> binding);

    StatefulOptionBuilder<V> setApplyHook(Consumer<ConfigState> hook);
}
