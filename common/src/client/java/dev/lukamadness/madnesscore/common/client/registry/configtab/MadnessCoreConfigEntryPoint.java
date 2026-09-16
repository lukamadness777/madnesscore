package dev.lukamadness.madnesscore.common.client.registry.configtab;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.api.config.ConfigEntryPoint;
import dev.lukamadness.madnesscore.common.client.api.config.structure.ConfigBuilder;
import dev.lukamadness.madnesscore.common.client.api.config.structure.OptionGroupBuilder;
import dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig;
import dev.lukamadness.madnesscore.common.client.customization.CustomizationScreen;
import dev.lukamadness.madnesscore.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class MadnessCoreConfigEntryPoint implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        String version = Services.PLATFORM.getModVersion(MadnessCoreCommon.MOD_ID).orElse("unknown");

        builder.registerModOptions(MadnessCoreCommon.MOD_ID, "Madness Core", version)
                .setColorTheme(builder.createColorTheme().setBaseThemeRGB(0x8B0000))
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("madnesscore.config.page.general"))
                        .addOptionGroup(this.buildGeneralGroup(builder)));
    }

    private OptionGroupBuilder buildGeneralGroup(ConfigBuilder builder) {
        return builder.createOptionGroup()
                .addOption(builder.createExternalButtonOption(id("open_customization_menu"))
                        .setName(Component.translatable("madnesscore.config.option.open_customization_menu.name"))
                        .setTooltip(Component.translatable("madnesscore.config.option.open_customization_menu.tooltip"))
                        .setScreenConsumer(screen -> Minecraft.getInstance().setScreen(
                                new CustomizationScreen(screen, CustomizationConfig.get()))));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, path);
    }
}
