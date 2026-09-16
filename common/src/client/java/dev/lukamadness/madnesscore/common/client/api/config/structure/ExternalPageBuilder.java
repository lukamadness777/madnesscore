package dev.lukamadness.madnesscore.common.client.api.config.structure;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public interface ExternalPageBuilder extends PageBuilder {
    ExternalPageBuilder setName(Component name);

    ExternalPageBuilder setScreenConsumer(Consumer<Screen> currentScreenConsumer);
}
