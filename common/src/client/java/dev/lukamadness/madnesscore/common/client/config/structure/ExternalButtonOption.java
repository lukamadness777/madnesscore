package dev.lukamadness.madnesscore.common.client.config.structure;

import dev.lukamadness.madnesscore.common.client.config.value.DependentValue;
import dev.lukamadness.madnesscore.common.client.gui.options.control.Control;
import dev.lukamadness.madnesscore.common.client.gui.options.control.ExternalButtonControl;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.Consumer;

public class ExternalButtonOption extends StaticOption {
    final Consumer<Screen> currentScreenConsumer;

    public ExternalButtonOption(
            ResourceLocation id,
            Collection<ResourceLocation> dependencies,
            Component name,
            DependentValue<Boolean> enabled,
            Component tooltip,
            Consumer<Screen> currentScreenConsumer
    ) {
        super(id, dependencies, name, enabled, tooltip);
        this.currentScreenConsumer = currentScreenConsumer;
    }

    @Override
    Control createControl() {
        return new ExternalButtonControl(this, this.currentScreenConsumer);
    }

    public Consumer<Screen> getCurrentScreenConsumer() {
        return this.currentScreenConsumer;
    }
}
