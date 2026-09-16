package dev.lukamadness.madnesscore.common.client.config.structure;

import com.google.common.collect.ImmutableList;
import dev.lukamadness.madnesscore.common.client.config.search.SearchIndex;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public record OptionPage(Component name, ImmutableList<OptionGroup> groups) implements Page {
    @Override
    public void registerTextSources(SearchIndex index, ModOptions modOptions) {
        for (OptionGroup group : this.groups()) {
            group.registerTextSources(index, modOptions, this);
        }
    }

    public List<Option.OptionNameSource> collectSources(ModOptions modOptions) {
        List<Option.OptionNameSource> sources = new ArrayList<>();
        for (OptionGroup group : this.groups()) {
            group.collectSources(modOptions, this, sources);
        }
        return sources;
    }
}
