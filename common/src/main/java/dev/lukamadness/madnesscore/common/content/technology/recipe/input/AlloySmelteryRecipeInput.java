// common/tecnology/recipe/input/AlloySmelteryRecipeInput.java
package dev.lukamadness.madnesscore.common.content.technology.recipe.input;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

/**
 * RecipeInput multi-slot para el Alloy Smeltery. Representa los 4 slots de INPUT
 * (no los de output) al buscar una receta que matchee.
 */
public class AlloySmelteryRecipeInput implements RecipeInput {

    public static final int SLOT_COUNT = 4;

    private final List<ItemStack> stacks;

    public AlloySmelteryRecipeInput(List<ItemStack> stacks) {
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