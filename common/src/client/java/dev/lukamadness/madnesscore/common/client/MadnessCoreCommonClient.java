package dev.lukamadness.madnesscore.common.client;

import dev.lukamadness.madnesscore.common.client.registry.bundledtab.ModBundledTabs;
import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.technology.*;
import dev.lukamadness.madnesscore.common.registry.menu.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * Init client-side compartido entre Fabric y NeoForge. Los mixins de
 * BundledTabs NO necesitan que nadie los llame acá (se cargan solos vía
 * madnesscore.mixins.json) — esto es para el registro real de datos:
 * BundledTabsAPI.registerGroup/addTab, y cualquier otra cosa client-only
 * que necesite un punto de entrada explícito (renderers, keybinds, etc.).
 * <p>
 * Llamar a {@link #init()} desde:
 * - fabric: la clase que implementa ClientModInitializer
 * - neoforge: un listener de FMLClientSetupEvent en el mod event bus
 */
public final class MadnessCoreCommonClient {
    private MadnessCoreCommonClient() {}

    public static void init() {
        MadnessCoreCommon.LOG.info("Initializing Madness Core client");
        ModBundledTabs.init();

        MenuScreens.register(ModMenus.HEAT_GENERATOR.get(), HeatGeneratorScreen::new);
        MenuScreens.register(ModMenus.ALLOY_SMELTERY.get(), AlloySmelteryScreen::new);
        MenuScreens.register(ModMenus.ENERGY_CONVERTER.get(), EnergyConverterScreen::new);

        // El sistema de slots (Trinkets-style) ya no tiene una pantalla propia que registrar:
        // sus slots dinamicos se inyectan directo en el inventario vainilla, ver
        // dev.lukamadness.madnesscore.common.mixin.MixinInventoryMenu y
        // dev.lukamadness.madnesscore.common.client.mixin.MixinInventoryScreen.
    }
}