package dev.lukamadness.madnesscore.common.client.config.builder;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import dev.lukamadness.madnesscore.common.client.api.config.ConfigState;
import dev.lukamadness.madnesscore.common.client.api.config.option.FlagHook;
import dev.lukamadness.madnesscore.common.client.api.config.structure.ColorThemeBuilder;
import dev.lukamadness.madnesscore.common.client.api.config.structure.ModOptionsBuilder;
import dev.lukamadness.madnesscore.common.client.api.config.structure.OptionBuilder;
import dev.lukamadness.madnesscore.common.client.api.config.structure.PageBuilder;
import dev.lukamadness.madnesscore.common.client.config.structure.*;
import dev.lukamadness.madnesscore.common.client.gui.ColorTheme;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

class ModOptionsBuilderImpl implements ModOptionsBuilder {
    private final String configId;
    private String name;
    private String version;
    private ColorTheme theme;
    private ResourceLocation icon;
    private boolean iconMonochrome = true;
    private final List<Page> pages = new ArrayList<>();
    private List<OptionOverride> optionOverrides;
    private List<OptionOverlay> optionOverlays;
    private Collection<FlagHook> flagHooks;

    ModOptionsBuilderImpl(String configId, String name, String version) {
        this.configId = configId;
        this.name = name;
        this.version = version;
    }

    ModOptions build() {
        Validate.notEmpty(this.name, "Name must not be empty");
        Validate.notEmpty(this.version, "Version must not be empty");

        var overrides = this.optionOverrides == null ? List.<OptionOverride>of() : this.optionOverrides;
        var overlays = this.optionOverlays == null ? List.<OptionOverlay>of() : this.optionOverlays;

        if (this.pages.isEmpty() && overrides.isEmpty() && overlays.isEmpty()) {
            throw new IllegalStateException("At least one page, option override, or option overlay must be added");
        }

        if (this.theme == null) {
            this.theme = ColorTheme.PRESETS[Math.abs(this.configId.hashCode()) % ColorTheme.PRESETS.length];
        }

        return new ModOptions(this.configId, this.name, this.version, this.theme, this.icon, this.iconMonochrome, ImmutableList.copyOf(this.pages), overrides, overlays, this.flagHooks);
    }

    @Override
    public ModOptionsBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ModOptionsBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public ModOptionsBuilder formatVersion(Function<String, String> versionFormatter) {
        this.version = versionFormatter.apply(this.version);
        return this;
    }

    @Override
    public ModOptionsBuilder setColorTheme(ColorThemeBuilder theme) {
        this.theme = ((ColorThemeBuilderImpl) theme).build();
        return this;
    }

    @Override
    public ModOptionsBuilder setIcon(ResourceLocation texture) {
        this.icon = texture;
        return this;
    }

    @Override
    public ModOptionsBuilder setNonTintedIcon(ResourceLocation texture) {
        this.icon = texture;
        this.iconMonochrome = false;
        return this;
    }

    @Override
    public ModOptionsBuilder addPage(PageBuilder builder) {
        this.pages.add(((PageBuilderImpl) builder).build());
        return this;
    }

    @Override
    public ModOptionsBuilder registerOptionReplacement(ResourceLocation target, OptionBuilder replacement) {
        return this.registerOptionReplacement(target, replacement, 0);
    }

    @Override
    public ModOptionsBuilder registerOptionReplacement(ResourceLocation target, OptionBuilder replacement, int priority) {
        var override = new OptionOverride(target, this.configId, ((OptionBuilderImpl<?>) replacement).build(), priority);
        if (this.optionOverrides == null) {
            this.optionOverrides = new ArrayList<>();
        }
        this.optionOverrides.add(override);
        return this;
    }

    @Override
    public ModOptionsBuilder registerOptionOverlay(ResourceLocation target, OptionBuilder overlay) {
        return this.registerOptionOverlay(target, overlay, 0);
    }

    @Override
    public ModOptionsBuilder registerOptionOverlay(ResourceLocation target, OptionBuilder overlay, int priority) {
        var optionOverlay = new OptionOverlay(target, this.configId, ((OptionBuilderImpl<?>) overlay), priority);
        if (this.optionOverlays == null) {
            this.optionOverlays = new ArrayList<>();
        }
        this.optionOverlays.add(optionOverlay);
        return this;
    }

    @Override
    public ModOptionsBuilder registerFlagHook(BiConsumer<Collection<ResourceLocation>, ConfigState> hook, ResourceLocation... triggers) {
        return this.registerFlagHook(new FlagHookImpl(hook, List.of(triggers)));
    }

    @Override
    public ModOptionsBuilder registerFlagHook(FlagHook hook) {
        if (this.flagHooks == null) {
            this.flagHooks = new ObjectArrayList<>();
        }
        this.flagHooks.add(hook);
        return this;
    }
}
