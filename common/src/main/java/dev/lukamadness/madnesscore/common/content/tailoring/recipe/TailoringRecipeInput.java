package dev.lukamadness.madnesscore.common.content.tailoring.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public class TailoringRecipeInput implements RecipeInput {
    public static final int SLOT_COUNT = 4;

    private final List<ItemStack> stacks;

    public TailoringRecipeInput(List<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public ItemStack getItem(int slot) {
        return stacks.get(slot);
    }

    @Override
    public int size() {
        return SLOT_COUNT;
    }
}
