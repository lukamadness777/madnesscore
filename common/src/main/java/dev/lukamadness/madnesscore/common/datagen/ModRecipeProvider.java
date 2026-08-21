package dev.lukamadness.madnesscore.common.datagen;

import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import dev.lukamadness.madnesscore.common.content.technology.recipe.AlloySmeltingRecipe;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Generador de recetas comun a Fabric y NeoForge.
 * <p>
 * Genera recetas para:
 * - Los 16 fabrics (dos formas: lana + hilo, o fabric existente + tinte).
 * - Conversion nugget <-> ingot para bronze y steel.
 * - Aleaciones EN EL ALLOY SMELTERY (no en mesa de crafteo): bronze ingot
 *   (cobre + oro) y steel ingot (hierro + carbon).
 * <p>
 * NO genera recetas para los plates (a pedido explicito).
 */
public class ModRecipeProvider extends RecipeProvider {

    // Orden identico al de DyeColor.values() (id 0 = WHITE ... id 15 = BLACK),
    // que es tambien el orden en que estan declarados los *_FABRIC en ModItems.
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
        buildAlloyRecipes(output);
    }

    private static void buildFabricRecipes(RecipeOutput output) {
        DyeColor[] colors = DyeColor.values();

        for (DyeColor color : colors) {
            int id = color.getId();
            Item fabric = FABRICS.get(id).get();
            Item wool = WOOLS.get(id);
            Item dye = DyeItem.byColor(color);

            // Receta 1: lana de color en el medio + hilo alrededor -> fabric de ese color
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, fabric)
                    .define('S', Items.STRING)
                    .define('W', wool)
                    .pattern("SSS")
                    .pattern("SWS")
                    .pattern("SSS")
                    .unlockedBy("has_wool", has(wool))
                    .save(output, RegistryHelper.id(color.getName() + "_fabric"));

            // Receta 2: cualquier OTRO fabric ya existente + tinte de este color -> fabric de este color
            // (no funciona con su propio tinte, ej. black_fabric + black_dye no es una receta valida)
            Item[] otherFabrics = new Item[colors.length - 1];
            int i = 0;
            for (DyeColor other : colors) {
                if (other != color) {
                    otherFabrics[i++] = FABRICS.get(other.getId()).get();
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
        // 9 nuggets -> 1 ingot
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ingot)
                .define('#', nugget)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .unlockedBy("has_" + name + "_nugget", has(nugget))
                .save(output, RegistryHelper.id(name + "_ingot_from_nuggets"));

        // 1 ingot -> 9 nuggets
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                .requires(ingot)
                .unlockedBy("has_" + name + "_ingot", has(ingot))
                .save(output, RegistryHelper.id(name + "_nugget_from_ingot"));
    }

    /**
     * Recetas del Alloy Smeltery (AlloySmeltingRecipe), NO de la mesa de crafteo: se
     * cargan directo al horno del bloque, no aparecen en el crafting grid. processtime
     * y heat_per_tick quedan en los defaults del propio recipe type (200 ticks, 40
     * Heat/tick) hasta que se pida un valor distinto para alguna de las dos.
     */
    private static void buildAlloyRecipes(RecipeOutput output) {
        // Bronze: 3 cobre + 1 oro -> 1 bronze ingot
        output.accept(
                RegistryHelper.id("bronze_ingot_from_copper_and_gold"),
                new AlloySmeltingRecipe(
                        List.of(
                                new AlloySmeltingRecipe.IngredientEntry(Ingredient.of(Items.COPPER_INGOT), 3),
                                new AlloySmeltingRecipe.IngredientEntry(Ingredient.of(Items.GOLD_INGOT), 1)
                        ),
                        List.of(new ItemStack(ModItems.BRONZE_INGOT.get())),
                        200,
                        40
                ),
                null
        );

        // Steel: 2 hierro + 1 carbon -> 1 steel ingot
        output.accept(
                RegistryHelper.id("steel_ingot_from_iron_and_coal"),
                new AlloySmeltingRecipe(
                        List.of(
                                new AlloySmeltingRecipe.IngredientEntry(Ingredient.of(Items.IRON_INGOT), 2),
                                new AlloySmeltingRecipe.IngredientEntry(Ingredient.of(Items.COAL), 1)
                        ),
                        List.of(new ItemStack(ModItems.STEEL_INGOT.get())),
                        200,
                        40
                ),
                null
        );
    }
}