package dev.lukamadness.madnesscore.common.content.tailoring.recipe;

import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record FormalSuitDyeInput(ItemStack suit, List<DyeItem> dyes, int modifierCount) {
    @Nullable
    public static FormalSuitDyeInput parse(CraftingInput input, @Nullable Item modifierItem) {
        ItemStack suit = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();
        int modifierCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() == ModItems.FORMAL_SUIT.get()) {
                if (!suit.isEmpty()) return null;
                suit = stack;
            } else if (stack.getItem() instanceof DyeItem dye) {
                dyes.add(dye);
            } else if (modifierItem != null && stack.is(modifierItem)) {
                modifierCount++;
            } else {
                return null;
            }
        }

        return suit.isEmpty() ? null : new FormalSuitDyeInput(suit, dyes, modifierCount);
    }
}
