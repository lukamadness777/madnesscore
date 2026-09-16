package dev.lukamadness.madnesscore.common.content.tailoring;

import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class FabricColors {
    private static final Map<Item, Integer> COLORS_BY_ITEM = new HashMap<>();
    private static final List<PendingColor> PENDING = new ArrayList<>();
    private static boolean initialized = false;

    private record PendingColor(Supplier<Item> item, int rgb) {
    }

    private FabricColors() {
    }

    private static void ensureInit() {
        if (initialized) return;
        initialized = true;

        register(ModItems.WHITE_FABRIC, DyeColor.WHITE);
        register(ModItems.ORANGE_FABRIC, DyeColor.ORANGE);
        register(ModItems.MAGENTA_FABRIC, DyeColor.MAGENTA);
        register(ModItems.LIGHT_BLUE_FABRIC, DyeColor.LIGHT_BLUE);
        register(ModItems.YELLOW_FABRIC, DyeColor.YELLOW);
        register(ModItems.LIME_FABRIC, DyeColor.LIME);
        register(ModItems.PINK_FABRIC, DyeColor.PINK);
        register(ModItems.GRAY_FABRIC, DyeColor.GRAY);
        register(ModItems.LIGHT_GRAY_FABRIC, DyeColor.LIGHT_GRAY);
        register(ModItems.CYAN_FABRIC, DyeColor.CYAN);
        register(ModItems.PURPLE_FABRIC, DyeColor.PURPLE);
        register(ModItems.BLUE_FABRIC, DyeColor.BLUE);
        register(ModItems.BROWN_FABRIC, DyeColor.BROWN);
        register(ModItems.GREEN_FABRIC, DyeColor.GREEN);
        register(ModItems.RED_FABRIC, DyeColor.RED);
        register(ModItems.BLACK_FABRIC, DyeColor.BLACK);

        for (PendingColor pending : PENDING) {
            COLORS_BY_ITEM.put(pending.item().get(), pending.rgb());
        }
        PENDING.clear();
    }

    private static void register(Supplier<Item> item, DyeColor color) {
        register(item, color.getTextureDiffuseColor());
    }

    public static void register(Supplier<Item> item, int rgb) {
        if (initialized) {
            COLORS_BY_ITEM.put(item.get(), rgb);
        } else {
            PENDING.add(new PendingColor(item, rgb));
        }
    }

    public static Optional<Integer> getColor(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        ensureInit();
        return Optional.ofNullable(COLORS_BY_ITEM.get(stack.getItem()));
    }

    public static boolean isColorableFabric(ItemStack stack) {
        return getColor(stack).isPresent();
    }
}
