package dev.lukamadness.madnesscore.common.registry.item;

import dev.lukamadness.madnesscore.common.platform.Services;
import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.registry.item.compat.ModDyeDepotItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModItems {

    // --- Tecnología (block items) ---
    public static final Supplier<Item> HEAT_GENERATOR = register("heat_generator",
            () -> new BlockItem(ModBlocks.HEAT_GENERATOR.get(), new Item.Properties()));

    public static final Supplier<Item> ALLOY_SMELTERY = register("alloy_smeltery",
            () -> new BlockItem(ModBlocks.ALLOY_SMELTERY.get(), new Item.Properties()));

    public static final Supplier<Item> ENERGY_CONVERTER = register("energy_converter",
            () -> new BlockItem(ModBlocks.ENERGY_CONVERTER.get(), new Item.Properties()));


    // Bronze
    public static final Supplier<Item> BRONZE_INGOT = register("bronze_ingot",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> BRONZE_NUGGET = register("bronze_nugget",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> BRONZE_PLATE = register("bronze_plate",
            () -> new Item(new Item.Properties()));

    // Iron
    public static final Supplier<Item> IRON_PLATE = register("iron_plate",
            () -> new Item(new Item.Properties()));

    // Steel
    public static final Supplier<Item> STEEL_INGOT = register("steel_ingot",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> STEEL_NUGGET = register("steel_nugget",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> STEEL_PLATE = register("steel_plate",
            () -> new Item(new Item.Properties()));

    // Fabric (dye color variants)
    public static final Supplier<Item> WHITE_FABRIC = register("white_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> ORANGE_FABRIC = register("orange_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> MAGENTA_FABRIC = register("magenta_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> LIGHT_BLUE_FABRIC = register("light_blue_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> YELLOW_FABRIC = register("yellow_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> LIME_FABRIC = register("lime_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> PINK_FABRIC = register("pink_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> GRAY_FABRIC = register("gray_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> LIGHT_GRAY_FABRIC = register("light_gray_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> CYAN_FABRIC = register("cyan_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> PURPLE_FABRIC = register("purple_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> BLUE_FABRIC = register("blue_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> BROWN_FABRIC = register("brown_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> GREEN_FABRIC = register("green_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> RED_FABRIC = register("red_fabric",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> BLACK_FABRIC = register("black_fabric",
            () -> new Item(new Item.Properties()));

    public static <T extends Item> Supplier<T> register(String id, Supplier<T> item) {
        return RegistryHelper.INSTANCE.registerItem(id, item);
    }

    public static void init() {
        if (Services.PLATFORM.isModLoaded("dye_depot")) {
            ModDyeDepotItems.init();
        }
    }
}