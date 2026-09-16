package dev.lukamadness.madnesscore.common.client.config.structure;

import com.google.common.collect.ImmutableList;
import dev.lukamadness.madnesscore.common.client.api.config.option.FlagHook;
import dev.lukamadness.madnesscore.common.client.config.search.SearchIndex;
import dev.lukamadness.madnesscore.common.client.config.search.Searchable;
import dev.lukamadness.madnesscore.common.client.gui.ColorTheme;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;

public record ModOptions(String configId, String name, String version, ColorTheme theme, ResourceLocation icon, boolean iconMonochrome, ImmutableList<Page> pages, List<OptionOverride> overrides, List<OptionOverlay> overlays, Collection<FlagHook> flagHooks) implements Searchable {
    @Override
    public void registerTextSources(SearchIndex index) {
        for (Page page : this.pages) {
            page.registerTextSources(index, this);
        }
    }
}
