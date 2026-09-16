
package dev.lukamadness.madnesscore.common.content.technology.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukamadness.madnesscore.common.registry.recipe.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class CompressingRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStack output;
    private final int processTime;
    private final int energyPerTick;

    public CompressingRecipe(Ingredient input, ItemStack output, int processTime, int energyPerTick) {
        this.input = input;
        this.output = output;
        this.processTime = processTime;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output;
    }

    public Ingredient getInput() { return input; }
    public ItemStack getOutput() { return output; }
    public int getProcessTime() { return processTime; }
    public int getEnergyPerTick() { return energyPerTick; }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipes.COMPRESSING_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.COMPRESSING.get();
    }

    public static class Serializer implements RecipeSerializer<CompressingRecipe> {
        public static final MapCodec<CompressingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(CompressingRecipe::getInput),
                ItemStack.CODEC.fieldOf("result").forGetter(CompressingRecipe::getOutput),
                Codec.INT.optionalFieldOf("processtime", 200).forGetter(CompressingRecipe::getProcessTime),
                Codec.INT.optionalFieldOf("energy_per_tick", 20).forGetter(CompressingRecipe::getEnergyPerTick)
        ).apply(instance, CompressingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CompressingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, CompressingRecipe::getInput,
                ItemStack.STREAM_CODEC, CompressingRecipe::getOutput,
                ByteBufCodecs.VAR_INT, CompressingRecipe::getProcessTime,
                ByteBufCodecs.VAR_INT, CompressingRecipe::getEnergyPerTick,
                CompressingRecipe::new
        );

        @Override
        public MapCodec<CompressingRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CompressingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
