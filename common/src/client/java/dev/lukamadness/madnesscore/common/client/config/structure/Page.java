package dev.lukamadness.madnesscore.common.client.config.structure;

import com.google.common.collect.ImmutableList;
import dev.lukamadness.madnesscore.common.client.config.search.SearchIndex;
import net.minecraft.network.chat.Component;

public interface Page {
    Component name();

    ImmutableList<OptionGroup> groups();

    void registerTextSources(SearchIndex index, ModOptions modOptions);
}
