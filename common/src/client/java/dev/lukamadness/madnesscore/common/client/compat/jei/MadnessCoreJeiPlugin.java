package dev.lukamadness.madnesscore.common.client.compat.jei;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.registry.recipe.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Collections;
import java.util.List;

@JeiPlugin
public class MadnessCoreJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CompressingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new AlloySmelteryRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new TailoringRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                CompressingRecipeCategory.RECIPE_TYPE,
                getVanillaRecipes(ModRecipes.COMPRESSING.get())
        );

        registration.addRecipes(
                AlloySmelteryRecipeCategory.RECIPE_TYPE,
                getVanillaRecipes(ModRecipes.ALLOY_SMELTING.get())
        );

        registration.addRecipes(
                TailoringRecipeCategory.RECIPE_TYPE,
                getVanillaRecipes(ModRecipes.TAILORING.get())
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.COMPRESSOR.get().asItem()), CompressingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ALLOY_SMELTERY.get().asItem()), AlloySmelteryRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TAILORING_TABLE.get().asItem()), TailoringRecipeCategory.RECIPE_TYPE);
    }

    private static <I extends RecipeInput, T extends Recipe<I>> List<T> getVanillaRecipes(RecipeType<T> type) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return Collections.emptyList();

        return client.level.getRecipeManager()
                .getAllRecipesFor(type)
                .stream()
                .map(RecipeHolder::value)
                .toList();
    }
}
