package dev.lukamadness.madnesscore.common.client.gui.options.control;

import dev.lukamadness.madnesscore.common.client.config.structure.Option;
import dev.lukamadness.madnesscore.common.client.gui.ColorTheme;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.client.gui.screens.Screen;

public interface Control {
    Option getOption();

    ControlElement createElement(Screen screen, AbstractOptionList list, Dim2i dim, ColorTheme theme);

    int getMaxWidth();
}
