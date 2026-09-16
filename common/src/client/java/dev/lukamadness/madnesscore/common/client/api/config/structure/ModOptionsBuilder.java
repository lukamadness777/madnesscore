package dev.lukamadness.madnesscore.common.client.api.config.structure;

import dev.lukamadness.madnesscore.common.client.api.config.ConfigState;
import dev.lukamadness.madnesscore.common.client.api.config.option.FlagHook;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionFlag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface ModOptionsBuilder {
    ModOptionsBuilder setName(String name);

    ModOptionsBuilder setVersion(String version);

    ModOptionsBuilder formatVersion(Function<String, String> versionFormatter);

    ModOptionsBuilder setColorTheme(ColorThemeBuilder colorTheme);

    ModOptionsBuilder setIcon(ResourceLocation texture);

    ModOptionsBuilder setNonTintedIcon(ResourceLocation texture);

    ModOptionsBuilder addPage(PageBuilder page);

    ModOptionsBuilder registerOptionReplacement(ResourceLocation target, OptionBuilder replacement);

    ModOptionsBuilder registerOptionReplacement(ResourceLocation target, OptionBuilder replacement, int priority);

    ModOptionsBuilder registerOptionOverlay(ResourceLocation target, OptionBuilder overlay);

    ModOptionsBuilder registerOptionOverlay(ResourceLocation target, OptionBuilder overlay, int priority);

    ModOptionsBuilder registerFlagHook(BiConsumer<Collection<ResourceLocation>, ConfigState> hook, ResourceLocation... triggers);

    ModOptionsBuilder registerFlagHook(FlagHook hook);
}
