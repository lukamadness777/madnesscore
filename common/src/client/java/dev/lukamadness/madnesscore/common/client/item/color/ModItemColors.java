package dev.lukamadness.madnesscore.common.client.item.color;

import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalColors;
import dev.lukamadness.madnesscore.common.registry.component.ModDataComponents;
import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;

public final class ModItemColors {
    private ModItemColors() {
    }

    @FunctionalInterface
    public interface ItemColorRegistrar {
        void register(ItemColor color, Item... items);
    }

    private static final int OPAQUE_WHITE = 0xFFFFFFFF;
    private static final int OPAQUE_DARK_GRAY = 0xFF1E1E1E;

    public static void registerAll(ItemColorRegistrar registrar) {
        ItemColor dyedColorTint = (stack, tintIndex) -> {
            DyedItemColor comp = stack.get(DataComponents.DYED_COLOR);
            return comp != null ? (comp.rgb() | 0xFF000000) : OPAQUE_WHITE;
        };
        registrar.register(dyedColorTint, ModItems.DYEABLE_SHIRT.get());
        registrar.register(dyedColorTint, ModItems.DYEABLE_JACKET.get());

        registrar.register((stack, tintIndex) -> {
            if (tintIndex != 1) return -1;
            DyedItemColor comp = stack.get(DataComponents.DYED_COLOR);
            return comp != null ? (comp.rgb() | 0xFF000000) : OPAQUE_WHITE;
        }, ModItems.DYEABLE_LEGGINS.get());

        registrar.register((stack, tintIndex) -> {
            FormalColors colors = stack.getOrDefault(ModDataComponents.FORMAL_COLORS.get(), FormalColors.DEFAULT);
            return switch (tintIndex) {
                case 0 -> colors.suitColor() | 0xFF000000;
                case 1 -> colors.shirtColor() | 0xFF000000;

                case 2 -> (colors.tieVisible() ? colors.tieColor() : colors.shirtColor()) | 0xFF000000;
                default -> -1;
            };
        }, ModItems.FORMAL_SUIT.get());

        registrar.register((stack, tintIndex) -> {
            if (tintIndex != 1) return -1;
            DyedItemColor comp = stack.get(DataComponents.DYED_COLOR);
            return comp != null ? (comp.rgb() | 0xFF000000) : OPAQUE_DARK_GRAY;
        }, ModItems.FORMAL_SUIT_BOOTS.get());
    }
}
