package dev.lukamadness.madnesscore.common.client.registry.bundledtab;

import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTab;
import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTabGroup;
import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTabsAPI;
import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.registry.creativetab.ModCreativeTabs;
import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Registro de ejemplo: le agrega dos bundle tabs ("Items" y "Bloques") a
 * {@link ModCreativeTabs#MADNESS_CORE_TAB}, usando la textura por defecto
 * de Madness Core. Sirve como referencia de cómo cualquier mod dependiente
 * usa {@link BundledTabsAPI} desde su propio client init -- Madness Core no
 * tiene ningún privilegio especial acá, solo es el primer "cliente" de su
 * propia API.
 */
public final class ModBundledTabs {
    private ModBundledTabs() {}

    public static void init() {
        // Textura fija por defecto (madnesscore:textures/gui/bundled_tabs/interface.png).
        // Para cambiarla dinámicamente (ej. según dimensión), pasar un
        // segundo Supplier<ResourceLocation> a registerGroup en vez de
        // usar este overload de un solo argumento.
        BundledTabGroup group = BundledTabsAPI.registerGroup(ModCreativeTabs.MADNESS_CORE_TAB);

        group.addTab(BundledTab.builder()
                .title(Component.translatable("bundledTab.madnesscore.tables"))
                .icon(() -> new ItemStack(ModItems.RED_FABRIC.get()))
                .displayItems((provider, output) -> {
                    output.accept(ModItems.HEAT_GENERATOR.get());
                    output.accept(ModItems.ALLOY_SMELTERY.get());
                    output.accept(ModItems.ENERGY_CONVERTER.get());
                })
                .build());

        group.addTab(BundledTab.builder()
                .title(Component.translatable("bundledTab.madnesscore.fabric"))
                .icon(() -> new ItemStack(ModItems.RED_FABRIC.get()))
                .displayItems((provider, output) -> {
                    output.accept(ModItems.WHITE_FABRIC.get());
                    output.accept(ModItems.ORANGE_FABRIC.get());
                    output.accept(ModItems.MAGENTA_FABRIC.get());
                    output.accept(ModItems.LIGHT_BLUE_FABRIC.get());
                    output.accept(ModItems.YELLOW_FABRIC.get());
                    output.accept(ModItems.LIME_FABRIC.get());
                    output.accept(ModItems.PINK_FABRIC.get());
                    output.accept(ModItems.GRAY_FABRIC.get());
                    output.accept(ModItems.LIGHT_GRAY_FABRIC.get());
                    output.accept(ModItems.CYAN_FABRIC.get());
                    output.accept(ModItems.PURPLE_FABRIC.get());
                    output.accept(ModItems.BLUE_FABRIC.get());
                    output.accept(ModItems.BROWN_FABRIC.get());
                    output.accept(ModItems.GREEN_FABRIC.get());
                    output.accept(ModItems.RED_FABRIC.get());
                    output.accept(ModItems.BLACK_FABRIC.get());
                })
                .build());

        group.addTab(BundledTab.builder()
                .title(Component.translatable("bundledTab.madnesscore.iron"))
                .icon(() -> new ItemStack(Items.IRON_INGOT))
                .displayItems((provider, output) -> {
                    output.accept(Items.IRON_INGOT);
                    output.accept(Items.IRON_NUGGET);
                    output.accept(ModItems.IRON_PLATE.get());
                })
                .build());

        group.addTab(BundledTab.builder()
                .title(Component.translatable("bundledTab.madnesscore.steel"))
                .icon(() -> new ItemStack(ModItems.STEEL_INGOT.get()))
                .displayItems((provider, output) -> {
                    output.accept(ModItems.STEEL_INGOT.get());
                    output.accept(ModItems.STEEL_NUGGET.get());
                    output.accept(ModItems.STEEL_PLATE.get());
                })
                .build());

        group.addTab(BundledTab.builder()
                .title(Component.translatable("bundledTab.madnesscore.bronze"))
                .icon(() -> new ItemStack(ModItems.BRONZE_INGOT.get()))
                .displayItems((provider, output) -> {
                    output.accept(ModItems.BRONZE_INGOT.get());
                    output.accept(ModItems.BRONZE_NUGGET.get());
                    output.accept(ModItems.BRONZE_PLATE.get());
                })
                .build());
    }
}