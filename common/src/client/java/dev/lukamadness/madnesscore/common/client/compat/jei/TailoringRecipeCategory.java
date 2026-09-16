package dev.lukamadness.madnesscore.common.client.compat.jei;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.content.tailoring.TailoringIngredient;
import dev.lukamadness.madnesscore.common.content.tailoring.recipe.TailoringRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class TailoringRecipeCategory implements IRecipeCategory<TailoringRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "tailoring");
    public static final RecipeType<TailoringRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, TailoringRecipe.class);

    private static final int WIDTH = 100;
    private static final int HEIGHT = 36;

    private static final int[][] INPUT_SLOT_POSITIONS = {
            {1, 1}, {19, 1}, {1, 19}, {19, 19}
    };
    private static final int OUTPUT_SLOT_X = 81;
    private static final int OUTPUT_SLOT_Y = 10;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable background;

    public TailoringRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.TAILORING_TABLE.get().asItem()));
        this.slot = guiHelper.getSlotDrawable();
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<TailoringRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("madnesscore.jei.category.tailoring");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void draw(TailoringRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        List<TailoringIngredient> requirements = recipe.getRequirements();
        for (int i = 0; i < INPUT_SLOT_POSITIONS.length && i < requirements.size(); i++) {
            int[] pos = INPUT_SLOT_POSITIONS[i];
            slot.draw(guiGraphics, pos[0] - 1, pos[1] - 1);
        }
        slot.draw(guiGraphics, OUTPUT_SLOT_X - 1, OUTPUT_SLOT_Y - 1);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TailoringRecipe recipe, IFocusGroup focuses) {
        List<TailoringIngredient> requirements = recipe.getRequirements();
        for (int i = 0; i < INPUT_SLOT_POSITIONS.length && i < requirements.size(); i++) {
            TailoringIngredient req = requirements.get(i);
            int[] pos = INPUT_SLOT_POSITIONS[i];

            ItemStack[] matching = req.ingredient().getItems();
            ItemStack[] withCount = Arrays.stream(matching)
                    .map(stack -> {
                        ItemStack copy = stack.copy();
                        copy.setCount(req.count());
                        return copy;
                    })
                    .toArray(ItemStack[]::new);

            builder.addSlot(RecipeIngredientRole.INPUT, pos[0], pos[1])
                    .addItemStacks(Arrays.asList(withCount));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y)
                .addItemStack(recipe.getOutput());
    }
}
