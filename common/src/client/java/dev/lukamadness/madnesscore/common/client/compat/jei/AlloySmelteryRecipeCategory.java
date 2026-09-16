package dev.lukamadness.madnesscore.common.client.compat.jei;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.content.technology.recipe.AlloySmeltingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
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

public class AlloySmelteryRecipeCategory implements IRecipeCategory<AlloySmeltingRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "alloy_smelting");
    public static final RecipeType<AlloySmeltingRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, AlloySmeltingRecipe.class);

    private static final int WIDTH = 116;
    private static final int HEIGHT = 36;

    private static final int[][] INPUT_SLOT_POSITIONS = {
            {1, 1}, {19, 1}, {1, 19}, {19, 19}
    };
    private static final int[][] OUTPUT_SLOT_POSITIONS = {
            {81, 1}, {99, 1}, {81, 19}, {99, 19}
    };

    private static final int ANIMATION_TICKS = 200;

    private static final int LIT_WIDTH = 24;
    private static final int LIT_HEIGHT = 16;

    private static final int LIT_X = 46;
    private static final int LIT_Y = 11;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable background;
    private final IDrawableStatic litBlank;
    private final IDrawableAnimated litProgress;

    public AlloySmelteryRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ALLOY_SMELTERY.get().asItem()));
        this.slot = guiHelper.getSlotDrawable();
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);

        ResourceLocation litBlankTexture = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID,
                "textures/gui/sprites/container/jei/industrial_smelter/lit_blank.png");
        ResourceLocation litProgressTexture = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID,
                "textures/gui/sprites/container/jei/industrial_smelter/lit_progress.png");

        this.litBlank = guiHelper.drawableBuilder(litBlankTexture, 0, 0, LIT_WIDTH, LIT_HEIGHT)
                .setTextureSize(LIT_WIDTH, LIT_HEIGHT)
                .build();
        IDrawableStatic litProgressStatic = guiHelper.drawableBuilder(litProgressTexture, 0, 0, LIT_WIDTH, LIT_HEIGHT)
                .setTextureSize(LIT_WIDTH, LIT_HEIGHT)
                .build();
        this.litProgress = guiHelper.createAnimatedDrawable(litProgressStatic, ANIMATION_TICKS,
                IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<AlloySmeltingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("madnesscore.jei.category.alloy_smelting");
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
    public void draw(AlloySmeltingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        for (int[] pos : INPUT_SLOT_POSITIONS) {
            slot.draw(guiGraphics, pos[0] - 1, pos[1] - 1);
        }
        for (int[] pos : OUTPUT_SLOT_POSITIONS) {
            slot.draw(guiGraphics, pos[0] - 1, pos[1] - 1);
        }

        litBlank.draw(guiGraphics, LIT_X, LIT_Y);
        litProgress.draw(guiGraphics, LIT_X, LIT_Y);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlloySmeltingRecipe recipe, IFocusGroup focuses) {
        List<AlloySmeltingRecipe.IngredientEntry> inputs = recipe.getInputs();
        for (int i = 0; i < INPUT_SLOT_POSITIONS.length; i++) {
            if (i >= inputs.size()) continue;
            AlloySmeltingRecipe.IngredientEntry entry = inputs.get(i);
            int[] pos = INPUT_SLOT_POSITIONS[i];

            ItemStack[] matching = entry.ingredient().getItems();
            ItemStack[] withCount = Arrays.stream(matching)
                    .map(stack -> {
                        ItemStack copy = stack.copy();
                        copy.setCount(entry.count());
                        return copy;
                    })
                    .toArray(ItemStack[]::new);

            builder.addSlot(RecipeIngredientRole.INPUT, pos[0], pos[1])
                    .addItemStacks(Arrays.asList(withCount));
        }

        List<ItemStack> outputs = recipe.getOutputs();
        for (int i = 0; i < OUTPUT_SLOT_POSITIONS.length; i++) {
            if (i >= outputs.size()) continue;
            int[] pos = OUTPUT_SLOT_POSITIONS[i];

            builder.addSlot(RecipeIngredientRole.OUTPUT, pos[0], pos[1])
                    .addItemStack(outputs.get(i));
        }
    }
}
