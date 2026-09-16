package dev.lukamadness.madnesscore.common.client.api.config.structure;

public interface ColorThemeBuilder {
    ColorThemeBuilder setBaseThemeRGB(int theme);

    ColorThemeBuilder setFullThemeRGB(int theme, int themeHighlight, int themeDisabled);
}
