package dev.lukamadness.madnesscore.common.registry.item;

import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableJacketItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableJacketItemFactory;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableLegginsItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableLegginsItemFactory;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableShirtItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableShirtItemFactory;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalColors;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitBootsItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitBootsItemFactory;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitItemFactory;
import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.registry.component.ModDataComponents;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import dev.lukamadness.madnesscore.common.registry.item.compat.ModDyeDepotItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class ModItems {
    public static final Supplier<Item> HEAT_GENERATOR = register("heat_generator",
            () -> new BlockItem(ModBlocks.HEAT_GENERATOR.get(), new Item.Properties()));

    public static final Supplier<Item> ALLOY_SMELTERY = register("alloy_smeltery",
            () -> new BlockItem(ModBlocks.ALLOY_SMELTERY.get(), new Item.Properties()));

    public static final Supplier<Item> ENERGY_CONVERTER = register("energy_converter",
            () -> new BlockItem(ModBlocks.ENERGY_CONVERTER.get(), new Item.Properties()));

    public static final Supplier<Item> COMPRESSOR = register("compressor",
            () -> new BlockItem(ModBlocks.COMPRESSOR.get(), new Item.Properties()));

    public static final Supplier<Item> TAILORING_TABLE = register("tailoring_table",
            () -> new BlockItem(ModBlocks.TAILORING_TABLE.get(), new Item.Properties()));

    public static final Supplier<Item> COPPER_PLATE = register("copper_plate",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> BRONZE_INGOT = register("bronze_ingot",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> BRONZE_NUGGET = register("bronze_nugget",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> BRONZE_PLATE = register("bronze_plate",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> IRON_PLATE = register("iron_plate",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> STEEL_INGOT = register("steel_ingot",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> STEEL_NUGGET = register("steel_nugget",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> STEEL_PLATE = register("steel_plate",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> GOLD_PLATE = register("gold_plate",
            () -> new Item(new Item.Properties()));

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

    private static final Optional<DyeableShirtItemFactory> DYEABLE_SHIRT_ITEM_FACTORY =
            loadClientFactory(DyeableShirtItemFactory.class);

    public static final Supplier<Item> DYEABLE_SHIRT = register("dyeable_shirt", () -> DYEABLE_SHIRT_ITEM_FACTORY
            .map(factory -> (Item) factory.create(ModArmorMaterials.CLOTHES_MATERIAL, new Item.Properties().stacksTo(1)))
            .orElseGet(() -> new DyeableShirtItem(ModArmorMaterials.CLOTHES_MATERIAL, new Item.Properties().stacksTo(1))));

    private static final Optional<DyeableJacketItemFactory> DYEABLE_JACKET_ITEM_FACTORY =
            loadClientFactory(DyeableJacketItemFactory.class);

    public static final Supplier<Item> DYEABLE_JACKET = register("dyeable_jacket", () -> DYEABLE_JACKET_ITEM_FACTORY
            .map(factory -> (Item) factory.create(ModArmorMaterials.CLOTHES_MATERIAL, new Item.Properties().stacksTo(1)))
            .orElseGet(() -> new DyeableJacketItem(ModArmorMaterials.CLOTHES_MATERIAL, new Item.Properties().stacksTo(1))));

    private static final Optional<DyeableLegginsItemFactory> DYEABLE_LEGGINS_ITEM_FACTORY =
            loadClientFactory(DyeableLegginsItemFactory.class);

    public static final Supplier<Item> DYEABLE_LEGGINS = register("dyeable_leggins", () -> DYEABLE_LEGGINS_ITEM_FACTORY
            .map(factory -> (Item) factory.create(ModArmorMaterials.CLOTHES_MATERIAL, new Item.Properties().stacksTo(1)))
            .orElseGet(() -> new DyeableLegginsItem(ModArmorMaterials.CLOTHES_MATERIAL, new Item.Properties().stacksTo(1))));

    private static final Optional<FormalSuitItemFactory> FORMAL_SUIT_ITEM_FACTORY =
            loadClientFactory(FormalSuitItemFactory.class);

    public static final Supplier<Item> FORMAL_SUIT = register("formal_suit", () -> {
        Item.Properties properties = new Item.Properties()
                .stacksTo(1)
                .component(ModDataComponents.FORMAL_COLORS.get(), FormalColors.DEFAULT);
        return FORMAL_SUIT_ITEM_FACTORY
                .map(factory -> (Item) factory.create(ModArmorMaterials.CLOTHES_MATERIAL, properties))
                .orElseGet(() -> new FormalSuitItem(ModArmorMaterials.CLOTHES_MATERIAL, properties));
    });

    private static final Optional<FormalSuitBootsItemFactory> FORMAL_SUIT_BOOTS_ITEM_FACTORY =
            loadClientFactory(FormalSuitBootsItemFactory.class);

    public static final Supplier<Item> FORMAL_SUIT_BOOTS = register("formal_suit_boots", () -> FORMAL_SUIT_BOOTS_ITEM_FACTORY
            .map(factory -> (Item) factory.create(ModArmorMaterials.BOOTS_MATERIAL, new Item.Properties().stacksTo(1)))
            .orElseGet(() -> new FormalSuitBootsItem(ModArmorMaterials.BOOTS_MATERIAL, new Item.Properties().stacksTo(1))));

    public static <T extends Item> Supplier<T> register(String id, Supplier<T> item) {
        return RegistryHelperLoader.INSTANCE.registerItem(id, item);
    }

    public static <T extends Item> Supplier<T> register(String namespace, String id, Supplier<T> item) {
        return RegistryHelperLoader.INSTANCE.registerItem(namespace, id, item);
    }

    public static <T> Optional<T> loadClientFactory(Class<T> factoryClass) {
        try {
            return ServiceLoader.load(factoryClass).findFirst();
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static void init() {
        if (ModDyeDepotItems.isEnabled()) {
            ModDyeDepotItems.init();
        }
    }
}
