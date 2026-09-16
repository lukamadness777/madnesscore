package dev.lukamadness.madnesscore.common.content.tailoring.recipe;

import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalColors;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitItem;
import dev.lukamadness.madnesscore.common.registry.recipe.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class FormalSuitJacketDyeRecipe extends CustomRecipe {
    public FormalSuitJacketDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        FormalSuitDyeInput parsed = FormalSuitDyeInput.parse(input, null);
        return parsed != null && !parsed.dyes().isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        FormalSuitDyeInput parsed = FormalSuitDyeInput.parse(input, null);
        if (parsed == null || parsed.dyes().isEmpty()) return ItemStack.EMPTY;

        ItemStack result = parsed.suit().copyWithCount(1);
        FormalColors current = FormalSuitItem.getColors(result);
        int newSuitColor = FormalSuitDyeBlend.blend(current.suitColor(), parsed.dyes());
        FormalSuitItem.setColors(result, new FormalColors(
                newSuitColor, current.tieColor(), current.shirtColor(), current.tieVisible()));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FORMAL_SUIT_JACKET_DYE_SERIALIZER.get();
    }
}
