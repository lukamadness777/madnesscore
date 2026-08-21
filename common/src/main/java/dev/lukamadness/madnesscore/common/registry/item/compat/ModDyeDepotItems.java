package dev.lukamadness.madnesscore.common.registry.item.compat;

import dev.lukamadness.madnesscore.common.platform.Services;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModDyeDepotItems {
    public static final String DYE_DEPOT_MOD_ID = "dye_depot";

    public static boolean isEnabled() {
        return Services.PLATFORM.isModLoaded(DYE_DEPOT_MOD_ID);
    }

    // ejemplo:
    // public static final Supplier<Item> SOME_COMPAT_ITEM = register("some_compat_item",
    //         () -> new Item(new Item.Properties()));

    private static <T extends Item> Supplier<T> register(String id, Supplier<T> item) {
        return RegistryHelper.INSTANCE.registerItem(id, item);
    }

    public static void init() {
        if (Services.PLATFORM.isModLoaded(ModDyeDepotItems.DYE_DEPOT_MOD_ID)) {
            ModDyeDepotItems.init();
        }
    }
}
