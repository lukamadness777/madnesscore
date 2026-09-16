package dev.lukamadness.madnesscore.common.client.registry.bundledtab;

import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTab;
import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTabGroup;
import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTabsAPI;
import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.registry.creativetab.ModCreativeTabs;
import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import dev.lukamadness.madnesscore.common.registry.item.compat.ModDyeDepotItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ModBundledTabs {
    private ModBundledTabs() {}

    public static void init() {
        BundledTabGroup group = BundledTabsAPI.registerGroup(ModCreativeTabs.MADNESS_CORE_TAB);

        group.addTab(BundledTab.builder()
                .title(Component.translatable("bundledTab.madnesscore.tables"))
                .icon(() -> new ItemStack(ModItems.ALLOY_SMELTERY.get()))
                .displayItems((provider, output) -> {
                    output.accept(ModItems.HEAT_GENERATOR.get());
                    output.accept(ModItems.ALLOY_SMELTERY.get());
                    output.accept(ModItems.ENERGY_CONVERTER.get());
                    output.accept(ModItems.COMPRESSOR.get());
                    output.accept(ModItems.TAILORING_TABLE.get());
                })
                .build());

        group.addTab(BundledTab.builder()
                .title(Component.translatable("bundledTab.madnesscore.fabric"))
                .icon(() -> new ItemStack(ModItems.RED_FABRIC.get()))
                .displayItems((provider, output) -> {
                    output.accept(ModItems.WHITE_FABRIC.get());
                    output.accept(ModItems.LIGHT_GRAY_FABRIC.get());
                    output.accept(ModItems.GRAY_FABRIC.get());
                    output.accept(ModItems.BLACK_FABRIC.get());
                    output.accept(ModItems.BROWN_FABRIC.get());

                    if (ModDyeDepotItems.isEnabled()) {
                        output.accept(ModDyeDepotItems.MAROON_FABRIC.get());
                        output.accept(ModDyeDepotItems.ROSE_FABRIC.get());
                    }

                    output.accept(ModItems.RED_FABRIC.get());

                    if (ModDyeDepotItems.isEnabled()) {
                        output.accept(ModDyeDepotItems.CORAL_FABRIC.get());
                        output.accept(ModDyeDepotItems.GINGER_FABRIC.get());
                    }

                    output.accept(ModItems.ORANGE_FABRIC.get());

                    if (ModDyeDepotItems.isEnabled()) {
                        output.accept(ModDyeDepotItems.TAN_FABRIC.get());
                        output.accept(ModDyeDepotItems.BEIGE_FABRIC.get());
                    }

                    output.accept(ModItems.YELLOW_FABRIC.get());

                    if (ModDyeDepotItems.isEnabled()) {
                        output.accept(ModDyeDepotItems.AMBER_FABRIC.get());
                        output.accept(ModDyeDepotItems.OLIVE_FABRIC.get());
                    }

                    output.accept(ModItems.LIME_FABRIC.get());

                    if (ModDyeDepotItems.isEnabled()) {
                        output.accept(ModDyeDepotItems.FOREST_FABRIC.get());
                    }

                    output.accept(ModItems.GREEN_FABRIC.get());

                    if (ModDyeDepotItems.isEnabled()) {
                        output.accept(ModDyeDepotItems.VERDANT_FABRIC.get());
                        output.accept(ModDyeDepotItems.TEAL_FABRIC.get());
                    }

                    output.accept(ModItems.CYAN_FABRIC.get());

                    if (ModDyeDepotItems.isEnabled()) {
                        output.accept(ModDyeDepotItems.MINT_FABRIC.get());
                        output.accept(ModDyeDepotItems.AQUA_FABRIC.get());
                    }

                    output.accept(ModItems.LIGHT_BLUE_FABRIC.get());
                    output.accept(ModItems.BLUE_FABRIC.get());

                    if (ModDyeDepotItems.isEnabled()) {
                        output.accept(ModDyeDepotItems.SLATE_FABRIC.get());
                        output.accept(ModDyeDepotItems.NAVY_FABRIC.get());
                        output.accept(ModDyeDepotItems.INDIGO_FABRIC.get());
                    }

                    output.accept(ModItems.PURPLE_FABRIC.get());
                    output.accept(ModItems.MAGENTA_FABRIC.get());
                    output.accept(ModItems.PINK_FABRIC.get());
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
                .title(Component.translatable("bundledTab.madnesscore.gold"))
                .icon(() -> new ItemStack(Items.GOLD_INGOT))
                .displayItems((provider, output) -> {
                    output.accept(Items.GOLD_INGOT);
                    output.accept(Items.GOLD_NUGGET);
                    output.accept(ModItems.GOLD_PLATE.get());
                })
                .build());

        group.addTab(BundledTab.builder()
                .title(Component.translatable("bundledTab.madnesscore.copper"))
                .icon(() -> new ItemStack(Items.COPPER_INGOT))
                .displayItems((provider, output) -> {
                    output.accept(Items.COPPER_INGOT);
                    BuiltInRegistries.ITEM
                            .getOptional(ResourceLocation.withDefaultNamespace("copper_nugget"))
                            .ifPresent(output::accept);
                    output.accept(ModItems.COPPER_PLATE.get());
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

        group.addTab(BundledTab.builder()
                .title(Component.translatable("bundledTab.madnesscore.clothes"))
                .icon(() -> new ItemStack(ModItems.BRONZE_INGOT.get()))
                .displayItems((provider, output) -> {
                    output.accept(ModItems.DYEABLE_SHIRT.get());
                    output.accept(ModItems.DYEABLE_LEGGINS.get());
                    output.accept(ModItems.DYEABLE_JACKET.get());
                })
                .build());
    }
}
