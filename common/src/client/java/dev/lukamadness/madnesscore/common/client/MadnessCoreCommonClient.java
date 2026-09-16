package dev.lukamadness.madnesscore.common.client;

import dev.lukamadness.madnesscore.common.client.config.ConfigManager;
import dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig;
import dev.lukamadness.madnesscore.common.client.menu.ScreenMenu;
import dev.lukamadness.madnesscore.common.client.registry.bundledtab.ModBundledTabs;
import dev.lukamadness.madnesscore.common.client.registry.configtab.MadnessCoreConfigEntryPoint;
import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.slots.render.VanillaArmorSlotRendererInit;
import dev.lukamadness.madnesscore.common.platform.Services;

public final class MadnessCoreCommonClient {
    private MadnessCoreCommonClient() {}

    public static void init() {
        MadnessCoreCommon.LOG.info("Initializing Madness Core client");
        ModBundledTabs.init();
        CustomizationConfig.load();
        registerOwnConfig();

        ScreenMenu.init();

        VanillaArmorSlotRendererInit.register();
    }

    public static void registerOwnConfig() {
        ConfigManager.setModInfoFunction(modId -> new ConfigManager.ModMetadata(
                modId,
                Services.PLATFORM.getModVersion(modId).orElse("unknown")));

        ConfigManager.registerConfigEntryPoint(MadnessCoreConfigEntryPoint::new, MadnessCoreCommon.MOD_ID);
    }

    public static void finalizeConfig() {
        ConfigManager.registerConfigsEarly();
        ConfigManager.registerConfigsLate();
    }
}
