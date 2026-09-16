package dev.lukamadness.madnesscore.common.client.api.config.structure;

import net.minecraft.resources.ResourceLocation;

public interface ConfigBuilder {
    ModOptionsBuilder registerModOptions(String configId, String name, String version);

    ModOptionsBuilder registerModOptions(String configId);

    ModOptionsBuilder registerOwnModOptions();

    ColorThemeBuilder createColorTheme();

    OptionPageBuilder createOptionPage();

    ExternalPageBuilder createExternalPage();

    OptionGroupBuilder createOptionGroup();

    BooleanOptionBuilder createBooleanOption(ResourceLocation id);

    IntegerOptionBuilder createIntegerOption(ResourceLocation id);

    <E extends Enum<E>> EnumOptionBuilder<E> createEnumOption(ResourceLocation id, Class<E> enumClass);

    ExternalButtonOptionBuilder createExternalButtonOption(ResourceLocation id);
}
