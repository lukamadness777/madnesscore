package dev.lukamadness.madnesscore.common.content.technology.heat;

import dev.lukamadness.madnesscore.common.registry.recipe.ModRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class HeatFuelRegistry {
    private HeatFuelRegistry() {}

    private static RecipeManager cachedManager;
    private static double cachedMaxTemperature = ModHeatStorage.AMBIENT_TEMPERATURE;

    public static Optional<HeatFuelRecipe> find(Level level, ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.HEAT_FUEL.get(), new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value);
    }

    public static double getMaxHeatTemperature(Level level) {
        RecipeManager manager = level.getRecipeManager();
        if (manager != cachedManager) {
            cachedManager = manager;
            cachedMaxTemperature = manager.getAllRecipesFor(ModRecipes.HEAT_FUEL.get()).stream()
                    .mapToDouble(holder -> holder.value().getTemperature())
                    .max()
                    .orElse(ModHeatStorage.AMBIENT_TEMPERATURE);
        }
        return cachedMaxTemperature;
    }
}
