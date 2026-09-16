package dev.lukamadness.madnesscore.common.client.api.config.option;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public enum OptionFlag {
    REQUIRES_RENDERER_RELOAD,

    REQUIRES_RENDERER_UPDATE,

    REQUIRES_ASSET_RELOAD,

    REQUIRES_VIDEOMODE_RELOAD,

    REQUIRES_GAME_RESTART;

    private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath("madnesscore", "builtin_option_flag." + this.name().toLowerCase(Locale.ROOT));

    public ResourceLocation getId() {
        return this.id;
    }
}
