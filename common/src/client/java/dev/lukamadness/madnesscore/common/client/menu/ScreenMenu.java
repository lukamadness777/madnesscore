package dev.lukamadness.madnesscore.common.client.menu;

import dev.lukamadness.madnesscore.common.client.menu.technology.AlloySmelteryScreen;
import dev.lukamadness.madnesscore.common.client.menu.technology.CompressorScreen;
import dev.lukamadness.madnesscore.common.client.menu.technology.EnergyConverterScreen;
import dev.lukamadness.madnesscore.common.client.menu.technology.HeatGeneratorScreen;
import dev.lukamadness.madnesscore.common.client.menu.tailoring.TailoringTableScreen;
import dev.lukamadness.madnesscore.common.registry.menu.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;

public class ScreenMenu {
    public static void init() {
        MenuScreens.register(ModMenus.HEAT_GENERATOR.get(), HeatGeneratorScreen::new);
        MenuScreens.register(ModMenus.ALLOY_SMELTERY.get(), AlloySmelteryScreen::new);
        MenuScreens.register(ModMenus.ENERGY_CONVERTER.get(), EnergyConverterScreen::new);
        MenuScreens.register(ModMenus.COMPRESSOR.get(), CompressorScreen::new);
        MenuScreens.register(ModMenus.TAILORING_TABLE.get(), TailoringTableScreen::new);
    }
}
