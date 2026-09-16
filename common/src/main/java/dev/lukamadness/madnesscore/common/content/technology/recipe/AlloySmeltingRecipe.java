package dev.lukamadness.madnesscore.common.content.technology.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukamadness.madnesscore.common.registry.recipe.ModRecipes;
import dev.lukamadness.madnesscore.common.content.technology.recipe.input.AlloySmelteryRecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class AlloySmeltingRecipe implements Recipe<AlloySmelteryRecipeInput> {
    public static final int MAX_INPUTS = 4;
    public static final int MAX_OUTPUTS = 4;

    private final List<IngredientEntry> inputs;
    private final List<ItemStack> outputs;
    private final int processTime;

    public AlloySmeltingRecipe(List<IngredientEntry> inputs, List<ItemStack> outputs, int processTime) {
        if (inputs.size() > MAX_INPUTS) throw new IllegalArgumentException("Máximo " + MAX_INPUTS + " inputs");
        if (outputs.size() > MAX_OUTPUTS) throw new IllegalArgumentException("Máximo " + MAX_OUTPUTS + " outputs");
        this.inputs = inputs;
        this.outputs = outputs;
        this.processTime = processTime;
    }

    public int[] findAssignment(AlloySmelteryRecipeInput input) {
        int[] assignment = new int[inputs.size()];
        boolean[] usedSlots = new boolean[input.size()];
        if (tryAssign(0, input, usedSlots, assignment)) {
            return assignment;
        }
        return null;
    }

    private boolean tryAssign(int ingredientIndex, AlloySmelteryRecipeInput recipeInput,
                              boolean[] usedSlots, int[] assignment) {
        if (ingredientIndex >= inputs.size()) return true;

        IngredientEntry entry = inputs.get(ingredientIndex);
        for (int slot = 0; slot < recipeInput.size(); slot++) {
            if (usedSlots[slot]) continue;
            ItemStack stack = recipeInput.getItem(slot);
            if (stack.isEmpty()) continue;
            if (entry.ingredient().test(stack) && stack.getCount() >= entry.count()) {
                usedSlots[slot] = true;
                assignment[ingredientIndex] = slot;
                if (tryAssign(ingredientIndex + 1, recipeInput, usedSlots, assignment)) {
                    return true;
                }
                usedSlots[slot] = false;
            }
        }
        return false;
    }

    @Override
    public boolean matches(AlloySmelteryRecipeInput input, Level level) {
        return findAssignment(input) != null;
    }

    @Override
    public ItemStack assemble(AlloySmelteryRecipeInput input, HolderLookup.Provider registries) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0);
    }

    public List<IngredientEntry> getInputs() { return inputs; }
    public List<ItemStack> getOutputs() { return outputs; }
    public int getProcessTime() { return processTime; }

    @Override
    public RecipeSerializer<? extends Recipe<AlloySmelteryRecipeInput>> getSerializer() {
        return ModRecipes.ALLOY_SMELTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<AlloySmelteryRecipeInput>> getType() {
        return ModRecipes.ALLOY_SMELTING.get();
    }

    public record IngredientEntry(Ingredient ingredient, int count) {
        public static final Codec<IngredientEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientEntry::ingredient),
                Codec.INT.optionalFieldOf("count", 1).forGetter(IngredientEntry::count)
        ).apply(instance, IngredientEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, IngredientEntry> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, IngredientEntry::ingredient,
                ByteBufCodecs.VAR_INT, IngredientEntry::count,
                IngredientEntry::new
        );
    }

    public static class Serializer implements RecipeSerializer<AlloySmeltingRecipe> {
        public static final MapCodec<AlloySmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientEntry.CODEC.listOf(1, MAX_INPUTS).fieldOf("inputs").forGetter(AlloySmeltingRecipe::getInputs),
                ItemStack.CODEC.listOf(1, MAX_OUTPUTS).fieldOf("outputs").forGetter(AlloySmeltingRecipe::getOutputs),
                Codec.INT.optionalFieldOf("processtime", 200).forGetter(AlloySmeltingRecipe::getProcessTime)
        ).apply(instance, AlloySmeltingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AlloySmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(ArrayList::new, IngredientEntry.STREAM_CODEC), AlloySmeltingRecipe::getInputs,
                ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC), AlloySmeltingRecipe::getOutputs,
                ByteBufCodecs.VAR_INT, AlloySmeltingRecipe::getProcessTime,
                AlloySmeltingRecipe::new
        );

        @Override
        public MapCodec<AlloySmeltingRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlloySmeltingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
