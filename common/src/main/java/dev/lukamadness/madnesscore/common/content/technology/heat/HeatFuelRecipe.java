package dev.lukamadness.madnesscore.common.content.technology.heat;

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

public class HeatFuelRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient item;
    private final double temperature;
    private final int burnTime;

    public HeatFuelRecipe(Ingredient item, double temperature, int burnTime) {
        this.item = item;
        this.temperature = temperature;
        this.burnTime = burnTime;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return item.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public Ingredient getItem() { return item; }

    public double getTemperature() { return temperature; }

    public int getBurnTime() { return burnTime; }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipes.HEAT_FUEL_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.HEAT_FUEL.get();
    }

    public static class Serializer implements RecipeSerializer<HeatFuelRecipe> {
        public static final MapCodec<HeatFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("item").forGetter(HeatFuelRecipe::getItem),
                Codec.DOUBLE.fieldOf("temperature").forGetter(HeatFuelRecipe::getTemperature),
                Codec.INT.optionalFieldOf("burn_time", 200).forGetter(HeatFuelRecipe::getBurnTime)
        ).apply(instance, HeatFuelRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, HeatFuelRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, HeatFuelRecipe::getItem,
                ByteBufCodecs.DOUBLE, HeatFuelRecipe::getTemperature,
                ByteBufCodecs.VAR_INT, HeatFuelRecipe::getBurnTime,
                HeatFuelRecipe::new
        );

        @Override
        public MapCodec<HeatFuelRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, HeatFuelRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
