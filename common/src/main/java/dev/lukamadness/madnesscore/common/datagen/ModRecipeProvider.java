package dev.lukamadness.madnesscore.common.datagen;

import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ModRecipeProvider extends RecipeProvider {
    private static final List<Item> WOOLS = List.of(
            Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL, Items.LIGHT_BLUE_WOOL,
            Items.YELLOW_WOOL, Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL,
            Items.LIGHT_GRAY_WOOL, Items.CYAN_WOOL, Items.PURPLE_WOOL, Items.BLUE_WOOL,
            Items.BROWN_WOOL, Items.GREEN_WOOL, Items.RED_WOOL, Items.BLACK_WOOL
    );

    private static final List<Supplier<Item>> FABRICS = List.of(
            ModItems.WHITE_FABRIC, ModItems.ORANGE_FABRIC, ModItems.MAGENTA_FABRIC, ModItems.LIGHT_BLUE_FABRIC,
            ModItems.YELLOW_FABRIC, ModItems.LIME_FABRIC, ModItems.PINK_FABRIC, ModItems.GRAY_FABRIC,
            ModItems.LIGHT_GRAY_FABRIC, ModItems.CYAN_FABRIC, ModItems.PURPLE_FABRIC, ModItems.BLUE_FABRIC,
            ModItems.BROWN_FABRIC, ModItems.GREEN_FABRIC, ModItems.RED_FABRIC, ModItems.BLACK_FABRIC
    );

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public  void buildRecipes(RecipeOutput output) {
        buildFabricRecipes(output);
        buildNuggetRecipes(output);
    }

    private static void buildFabricRecipes(RecipeOutput output) {
        for (DyeColor color : DyeColor.values()) {
            int id = color.getId();

            if (id >= FABRICS.size()) {
                continue;
            }

            Item fabric = FABRICS.get(id).get();
            Item wool = WOOLS.get(id);
            Item dye = DyeItem.byColor(color);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, fabric)
                    .define('S', Items.STRING)
                    .define('W', wool)
                    .define('T', Items.STICK)
                    .pattern("STS")
                    .pattern("SWS")
                    .pattern("STS")
                    .unlockedBy("has_wool", has(wool))
                    .save(output, RegistryHelper.id(color.getName() + "_fabric"));

            Item[] otherFabrics = new Item[FABRICS.size() - 1];
            int i = 0;
            for (int otherId = 0; otherId < FABRICS.size(); otherId++) {
                if (otherId != id) {
                    otherFabrics[i++] = FABRICS.get(otherId).get();
                }
            }

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, fabric)
                    .requires(Ingredient.of(otherFabrics))
                    .requires(dye)
                    .unlockedBy("has_dye", has(dye))
                    .save(output, RegistryHelper.id(color.getName() + "_fabric_from_dye"));
        }
    }

    private static void buildNuggetRecipes(RecipeOutput output) {
        nuggetRecipes(output, ModItems.BRONZE_NUGGET.get(), ModItems.BRONZE_INGOT.get(), "bronze");
        nuggetRecipes(output, ModItems.STEEL_NUGGET.get(), ModItems.STEEL_INGOT.get(), "steel");
    }

    private static void nuggetRecipes(RecipeOutput output, Item nugget, Item ingot, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ingot)
                .define('#', nugget)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .unlockedBy("has_" + name + "_nugget", has(nugget))
                .save(output, RegistryHelper.id(name + "_ingot_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                .requires(ingot)
                .unlockedBy("has_" + name + "_ingot", has(ingot))
                .save(output, RegistryHelper.id(name + "_nugget_from_ingot"));
    }
}
