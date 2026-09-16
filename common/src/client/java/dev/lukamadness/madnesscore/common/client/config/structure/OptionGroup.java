package dev.lukamadness.madnesscore.common.client.config.structure;

import dev.lukamadness.madnesscore.common.client.config.search.SearchIndex;
import net.minecraft.network.chat.Component;

import java.util.List;

public record OptionGroup(Component name, List<Option> options) {
    public void registerTextSources(SearchIndex index, ModOptions modOptions, OptionPage page) {
        for (Option option : this.options) {
            option.registerTextSources(index, modOptions, page, this);
        }
    }

    public void collectSources(ModOptions modOptions, OptionPage page, List<Option.OptionNameSource> out) {
        for (Option option : this.options) {
            out.add(option.createNameSource(modOptions, page, this));
        }
    }
}
