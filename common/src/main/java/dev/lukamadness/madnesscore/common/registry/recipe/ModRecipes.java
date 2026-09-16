package dev.lukamadness.madnesscore.common.registry.recipe;

import dev.lukamadness.madnesscore.common.content.tailoring.recipe.FormalSuitJacketDyeRecipe;
import dev.lukamadness.madnesscore.common.content.tailoring.recipe.FormalSuitShirtDyeRecipe;
import dev.lukamadness.madnesscore.common.content.tailoring.recipe.FormalSuitTieDyeRecipe;
import dev.lukamadness.madnesscore.common.content.tailoring.recipe.FormalSuitTieToggleRecipe;
import dev.lukamadness.madnesscore.common.content.tailoring.recipe.TailoringRecipe;
import dev.lukamadness.madnesscore.common.content.technology.recipe.AlloySmeltingRecipe;
import dev.lukamadness.madnesscore.common.content.technology.recipe.CompressingRecipe;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFuelRecipe;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import java.util.function.Supplier;

public class ModRecipes {
    public static final Supplier<RecipeType<AlloySmeltingRecipe>> ALLOY_SMELTING =
            RegistryHelperLoader.INSTANCE.registerRecipeType("alloy_smelting");

    public static final Supplier<AlloySmeltingRecipe.Serializer> ALLOY_SMELTING_SERIALIZER =
            RegistryHelperLoader.INSTANCE.registerRecipeSerializer("alloy_smelting", AlloySmeltingRecipe.Serializer::new);

    public static final Supplier<RecipeType<CompressingRecipe>> COMPRESSING =
            RegistryHelperLoader.INSTANCE.registerRecipeType("compressing");

    public static final Supplier<CompressingRecipe.Serializer> COMPRESSING_SERIALIZER =
            RegistryHelperLoader.INSTANCE.registerRecipeSerializer("compressing", CompressingRecipe.Serializer::new);

    public static final Supplier<RecipeType<HeatFuelRecipe>> HEAT_FUEL =
            RegistryHelperLoader.INSTANCE.registerRecipeType("heat_fuel");

    public static final Supplier<HeatFuelRecipe.Serializer> HEAT_FUEL_SERIALIZER =
            RegistryHelperLoader.INSTANCE.registerRecipeSerializer("heat_fuel", HeatFuelRecipe.Serializer::new);

    public static final Supplier<RecipeType<TailoringRecipe>> TAILORING =
            RegistryHelperLoader.INSTANCE.registerRecipeType("tailoring");

    public static final Supplier<TailoringRecipe.Serializer> TAILORING_SERIALIZER =
            RegistryHelperLoader.INSTANCE.registerRecipeSerializer("tailoring", TailoringRecipe.Serializer::new);

    public static final Supplier<SimpleCraftingRecipeSerializer<FormalSuitJacketDyeRecipe>> FORMAL_SUIT_JACKET_DYE_SERIALIZER =
            RegistryHelperLoader.INSTANCE.registerRecipeSerializer("formal_suit_jacket_dye",
                    () -> new SimpleCraftingRecipeSerializer<>(FormalSuitJacketDyeRecipe::new));

    public static final Supplier<SimpleCraftingRecipeSerializer<FormalSuitShirtDyeRecipe>> FORMAL_SUIT_SHIRT_DYE_SERIALIZER =
            RegistryHelperLoader.INSTANCE.registerRecipeSerializer("formal_suit_shirt_dye",
                    () -> new SimpleCraftingRecipeSerializer<>(FormalSuitShirtDyeRecipe::new));

    public static final Supplier<SimpleCraftingRecipeSerializer<FormalSuitTieDyeRecipe>> FORMAL_SUIT_TIE_DYE_SERIALIZER =
            RegistryHelperLoader.INSTANCE.registerRecipeSerializer("formal_suit_tie_dye",
                    () -> new SimpleCraftingRecipeSerializer<>(FormalSuitTieDyeRecipe::new));

    public static final Supplier<SimpleCraftingRecipeSerializer<FormalSuitTieToggleRecipe>> FORMAL_SUIT_TIE_TOGGLE_SERIALIZER =
            RegistryHelperLoader.INSTANCE.registerRecipeSerializer("formal_suit_tie_toggle",
                    () -> new SimpleCraftingRecipeSerializer<>(FormalSuitTieToggleRecipe::new));

    public static void init() {
    }
}
