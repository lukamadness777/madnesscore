package dev.lukamadness.madnesscore.common.client.compat.jei;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.content.technology.recipe.CompressingRecipe;
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

public class CompressingRecipeCategory implements IRecipeCategory<CompressingRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "compressing");
    public static final RecipeType<CompressingRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, CompressingRecipe.class);

    private static final int WIDTH = 66;
    private static final int HEIGHT = 18;

    private static final int INPUT_SLOT_X = 1;
    private static final int INPUT_SLOT_Y = 1;
    private static final int OUTPUT_SLOT_X = 49;
    private static final int OUTPUT_SLOT_Y = 1;

    private static final int ANIMATION_TICKS = 200;

    private static final int LIT_X = 24;
    private static final int LIT_Y = 0;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable background;
    private final IDrawableStatic litBlank;
    private final IDrawableAnimated litProgress;

    public CompressingRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.COMPRESSOR.get().asItem()));
        this.slot = guiHelper.getSlotDrawable();
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);

        ResourceLocation litBlankTexture = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID,
                "textures/gui/sprites/container/jei/compressor/lit_blank.png");
        ResourceLocation litProgressTexture = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID,
                "textures/gui/sprites/container/jei/compressor/lit_progress.png");

        this.litBlank = guiHelper.drawableBuilder(litBlankTexture, 0, 0, 18, 18)
                .setTextureSize(18, 18)
                .build();
        IDrawableStatic litProgressStatic = guiHelper.drawableBuilder(litProgressTexture, 0, 0, 18, 18)
                .setTextureSize(18, 18)
                .build();

        this.litProgress = guiHelper.createAnimatedDrawable(litProgressStatic, ANIMATION_TICKS,
                IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<CompressingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("madnesscore.jei.category.compressing");
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
    public void draw(CompressingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        slot.draw(guiGraphics, INPUT_SLOT_X - 1, INPUT_SLOT_Y - 1);
        slot.draw(guiGraphics, OUTPUT_SLOT_X - 1, OUTPUT_SLOT_Y - 1);

        litBlank.draw(guiGraphics, LIT_X, LIT_Y);
        litProgress.draw(guiGraphics, LIT_X, LIT_Y);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CompressingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_SLOT_X, INPUT_SLOT_Y)
                .addItemStacks(Arrays.asList(recipe.getInput().getItems()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y)
                .addItemStack(recipe.getOutput());
    }
}
