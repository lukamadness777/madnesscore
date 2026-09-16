package dev.lukamadness.madnesscore.common.registry.item.compat;

import dev.lukamadness.madnesscore.common.content.tailoring.FabricColors;
import dev.lukamadness.madnesscore.common.platform.Services;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModDyeDepotItems {
    public static final String DYE_DEPOT_MOD_ID = "dye_depot";

    public static boolean isEnabled() {
        return Services.PLATFORM.isModLoaded(DYE_DEPOT_MOD_ID);
    }

    public static final Supplier<Item> MAROON_FABRIC = register("maroon_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> ROSE_FABRIC = register("rose_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> CORAL_FABRIC = register("coral_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> GINGER_FABRIC = register("ginger_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> TAN_FABRIC = register("tan_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> BEIGE_FABRIC = register("beige_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> AMBER_FABRIC = register("amber_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> OLIVE_FABRIC = register("olive_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> FOREST_FABRIC = register("forest_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> VERDANT_FABRIC = register("verdant_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> TEAL_FABRIC = register("teal_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> MINT_FABRIC = register("mint_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> AQUA_FABRIC = register("aqua_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> SLATE_FABRIC = register("slate_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> NAVY_FABRIC = register("navy_fabric",
            () -> new Item(new Item.Properties()));
    public static final Supplier<Item> INDIGO_FABRIC = register("indigo_fabric",
            () -> new Item(new Item.Properties()));

    private static <T extends Item> Supplier<T> register(String id, Supplier<T> item) {
        return RegistryHelperLoader.INSTANCE.registerItem(id, item);
    }

    public static void init() {
        if (!isEnabled()) return;

        FabricColors.register(MAROON_FABRIC, 0x7B2713);
        FabricColors.register(ROSE_FABRIC, 0xFF5E64);
        FabricColors.register(CORAL_FABRIC, 0xDF7758);
        FabricColors.register(INDIGO_FABRIC, 0x331E57);
        FabricColors.register(NAVY_FABRIC, 0x153D64);
        FabricColors.register(SLATE_FABRIC, 0x4C5E86);
        FabricColors.register(OLIVE_FABRIC, 0x8C8F2A);
        FabricColors.register(AMBER_FABRIC, 0xD7AF00);
        FabricColors.register(BEIGE_FABRIC, 0xE1D5A3);
        FabricColors.register(TEAL_FABRIC, 0x2F7B67);
        FabricColors.register(MINT_FABRIC, 0x38CE7D);
        FabricColors.register(AQUA_FABRIC, 0x5EF0CC);
        FabricColors.register(VERDANT_FABRIC, 0x255714);
        FabricColors.register(FOREST_FABRIC, 0x32A326);
        FabricColors.register(GINGER_FABRIC, 0xCF6121);
        FabricColors.register(TAN_FABRIC, 0xF49C5D);
    }
}
