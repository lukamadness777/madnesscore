// common/registry/recipe/ModRecipes.java
package dev.lukamadness.madnesscore.common.registry.recipe;

import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.content.technology.recipe.AlloySmeltingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

/**
 * RecipeType y RecipeSerializer, igual que bloques/items, pasan por RegistryHelper:
 * en NeoForge (a partir de esta versión) BuiltInRegistries.RECIPE_TYPE/RECIPE_SERIALIZER
 * ya están frozen para cuando corre el constructor del mod si se usa Registry.register
 * directo — hace falta DeferredRegister + RegisterEvent. En Fabric el registro directo
 * sigue andando igual.
 */
public class ModRecipes {

    public static final Supplier<RecipeType<AlloySmeltingRecipe>> ALLOY_SMELTING =
            RegistryHelper.INSTANCE.registerRecipeType("alloy_smelting");

    public static final Supplier<AlloySmeltingRecipe.Serializer> ALLOY_SMELTING_SERIALIZER =
            RegistryHelper.INSTANCE.registerRecipeSerializer("alloy_smelting", AlloySmeltingRecipe.Serializer::new);

    public static void init() {
        // Fuerza la carga de la clase para que los registros de arriba corran.
    }
}