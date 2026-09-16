package dev.lukamadness.madnesscore.common.client.api.config.structure;

import dev.lukamadness.madnesscore.common.client.api.config.ConfigState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ExternalButtonOptionBuilder extends OptionBuilder {
    @Override
    ExternalButtonOptionBuilder setName(Component name);

    @Override
    ExternalButtonOptionBuilder setTooltip(Component tooltip);

    @Override
    ExternalButtonOptionBuilder setEnabled(boolean available);

    @Override
    ExternalButtonOptionBuilder setEnabledProvider(Function<ConfigState, Boolean> provider, ResourceLocation... dependencies);

    ExternalButtonOptionBuilder setScreenConsumer(Consumer<Screen> currentScreenConsumer);
}
