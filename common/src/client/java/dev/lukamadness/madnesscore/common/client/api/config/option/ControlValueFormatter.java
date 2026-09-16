package dev.lukamadness.madnesscore.common.client.api.config.option;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface ControlValueFormatter {
    Component format(int value);
}
