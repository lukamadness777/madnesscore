package dev.lukamadness.madnesscore.common.client.config.builder;

import dev.lukamadness.madnesscore.common.client.api.config.ConfigState;
import dev.lukamadness.madnesscore.common.client.api.config.StorageEventHandler;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionBinding;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionFlag;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionImpact;
import dev.lukamadness.madnesscore.common.client.api.config.structure.BooleanOptionBuilder;
import dev.lukamadness.madnesscore.common.client.config.structure.BooleanOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class BooleanOptionBuilderImpl extends StatefulOptionBuilderImpl<BooleanOption, Boolean> implements BooleanOptionBuilder {
    BooleanOptionBuilderImpl(ResourceLocation id) {
        super(id);
    }

    @Override
    BooleanOption build() {
        this.prepareBuild();

        return new BooleanOption(
                this.id,
                this.getDependencies(),
                this.getName(),
                this.getEnabled(),
                this.getStorage(),
                this.getTooltipProvider(),
                this.getImpact(),
                this.getFlags(),
                this.getDefaultValue(),
                this.getControlHiddenWhenDisabled(),
                this.getBinding(),
                this.getApplyHook());
    }

    @Override
    Class<BooleanOption> getOptionClass() {
        return BooleanOption.class;
    }

    @Override
    public BooleanOptionBuilder setName(Component name) {
        super.setName(name);
        return this;
    }

    @Override
    public BooleanOptionBuilder setStorageHandler(StorageEventHandler storage) {
        super.setStorageHandler(storage);
        return this;
    }

    @Override
    public BooleanOptionBuilder setTooltip(Component tooltip) {
        super.setTooltip(tooltip);
        return this;
    }

    @Override
    public BooleanOptionBuilder setTooltip(Function<Boolean, Component> tooltip) {
        super.setTooltip(tooltip);
        return this;
    }

    @Override
    public BooleanOptionBuilder setImpact(OptionImpact impact) {
        super.setImpact(impact);
        return this;
    }

    @Override
    public BooleanOptionBuilder setFlags(OptionFlag... flags) {
        super.setFlags(flags);
        return this;
    }

    @Override
    public BooleanOptionBuilder setFlags(ResourceLocation... flags) {
        super.setFlags(flags);
        return this;
    }

    @Override
    public BooleanOptionBuilder setDefaultValue(Boolean value) {
        super.setDefaultValue(value);
        return this;
    }

    @Override
    public BooleanOptionBuilder setDefaultProvider(Function<ConfigState, Boolean> provider, ResourceLocation... dependencies) {
        super.setDefaultProvider(provider, dependencies);
        return this;
    }

    @Override
    public BooleanOptionBuilder setEnabled(boolean available) {
        super.setEnabled(available);
        return this;
    }

    @Override
    public BooleanOptionBuilder setEnabledProvider(Function<ConfigState, Boolean> provider, ResourceLocation... dependencies) {
        super.setEnabledProvider(provider, dependencies);
        return this;
    }

    @Override
    public BooleanOptionBuilder setControlHiddenWhenDisabled(boolean hidden) {
        super.setControlHiddenWhenDisabled(hidden);
        return this;
    }

    @Override
    public BooleanOptionBuilder setBinding(Consumer<Boolean> save, Supplier<Boolean> load) {
        super.setBinding(save, load);
        return this;
    }

    @Override
    public BooleanOptionBuilder setBinding(OptionBinding<Boolean> binding) {
        super.setBinding(binding);
        return this;
    }

    @Override
    public BooleanOptionBuilder setApplyHook(Consumer<ConfigState> hook) {
        super.setApplyHook(hook);
        return this;
    }
}
