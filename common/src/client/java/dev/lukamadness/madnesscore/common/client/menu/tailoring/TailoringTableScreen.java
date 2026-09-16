package dev.lukamadness.madnesscore.common.client.menu.tailoring;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.tailoring.recipe.TailoringRecipe;
import dev.lukamadness.madnesscore.common.content.tailoring.screen.TailoringTableScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TailoringTableScreen extends AbstractContainerScreen<TailoringTableScreenHandler> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/container/tailoring_table.png");

    private static final ResourceLocation PATTERN =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "container/tailor/pattern");
    private static final ResourceLocation PATTERN_HIGHLIGHTED =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "container/tailor/pattern_highlighted");
    private static final ResourceLocation PATTERN_SELECTED =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "container/tailor/pattern_selected");
    private static final ResourceLocation SCROLLER =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "container/tailor/scroller");
    private static final ResourceLocation SCROLLER_DISABLED =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "container/tailor/scroller_disabled");

    private static final int LIST_X = 60;
    private static final int LIST_Y = 17;
    private static final int LIST_WIDTH = 56;
    private static final int LIST_HEIGHT = 52;
    private static final int LIST_COLUMNS = 4;
    private static final int ICON_SIZE = 14;
    private static final int SCROLL_STEP = ICON_SIZE / 2;

    private static final int SCROLLBAR_X = LIST_X + LIST_WIDTH + 4;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;

    private float scrollOffset = 0f;
    private boolean draggingScrollbar = false;
    private int dragOffsetY = 0;

    public TailoringTableScreen(TailoringTableScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        menu.registerUpdateListener(this::containerChanged);
    }

    private void containerChanged() {
        this.scrollOffset = Math.min(scrollOffset, getMaxScrollPixels());
    }

    private int getTotalRows() {
        return (int) Math.ceil(menu.getNumRecipes() / (double) LIST_COLUMNS);
    }

    private float getMaxScrollPixels() {
        return Math.max(0, getTotalRows() * ICON_SIZE - LIST_HEIGHT);
    }

    private int getThumbY() {
        float maxScroll = getMaxScrollPixels();
        if (maxScroll <= 0) return LIST_Y;
        float progress = scrollOffset / maxScroll;
        return LIST_Y + Math.round(progress * (LIST_HEIGHT - SCROLLBAR_HEIGHT));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.scrollOffset = clamp(this.scrollOffset, 0f, getMaxScrollPixels());

        int x = leftPos;
        int y = topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        var recipes = menu.getCandidates();

        int pixelOffset = Math.round(scrollOffset);
        int firstRow = Math.max(0, pixelOffset / ICON_SIZE);
        int lastRow = Math.min(getTotalRows(), (pixelOffset + LIST_HEIGHT) / ICON_SIZE + 1);

        guiGraphics.enableScissor(x + LIST_X, y + LIST_Y, x + LIST_X + LIST_WIDTH, y + LIST_Y + LIST_HEIGHT);
        for (int row = firstRow; row < lastRow; row++) {
            for (int col = 0; col < LIST_COLUMNS; col++) {
                int slot = row * LIST_COLUMNS + col;
                if (slot >= recipes.size()) continue;

                int slotX = LIST_X + col * ICON_SIZE;
                int slotY = LIST_Y + row * ICON_SIZE - pixelOffset;

                ResourceLocation sprite;
                if (slot == menu.getSelectedRecipeIndex()) {
                    sprite = PATTERN_SELECTED;
                } else if (isHovering(slotX, slotY, ICON_SIZE, ICON_SIZE, mouseX, mouseY)
                        && isHovering(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT, mouseX, mouseY)) {
                    sprite = PATTERN_HIGHLIGHTED;
                } else {
                    sprite = PATTERN;
                }

                guiGraphics.blitSprite(sprite, x + slotX, y + slotY, ICON_SIZE, ICON_SIZE);

                TailoringRecipe.Candidate candidate = recipes.get(slot);
                guiGraphics.renderItem(candidate.result(), x + slotX - 1, y + slotY - 1);
            }
        }
        guiGraphics.disableScissor();

        boolean scrollable = getMaxScrollPixels() > 0;
        ResourceLocation scrollbarSprite = scrollable ? SCROLLER : SCROLLER_DISABLED;
        guiGraphics.blitSprite(scrollbarSprite, x + SCROLLBAR_X, y + getThumbY(), SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;
        int relX = mouseX - x;
        int relY = mouseY - y;
        if (isHovering(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT, mouseX, mouseY)) {
            int pixelOffset = Math.round(scrollOffset);
            int row = (relY - LIST_Y + pixelOffset) / ICON_SIZE;
            int col = (relX - LIST_X) / ICON_SIZE;
            int slot = row * LIST_COLUMNS + col;
            var recipes = menu.getCandidates();
            if (col >= 0 && col < LIST_COLUMNS && slot >= 0 && slot < recipes.size()) {
                guiGraphics.renderTooltip(font, recipes.get(slot).result(), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = leftPos;
        int y = topPos;

        if (getMaxScrollPixels() > 0 && isHovering(SCROLLBAR_X, LIST_Y, SCROLLBAR_WIDTH, LIST_HEIGHT, mouseX, mouseY)) {
            this.draggingScrollbar = true;
            int currentThumbY = getThumbY();
            double relativeMouseY = mouseY - y;
            if (relativeMouseY >= currentThumbY && relativeMouseY < currentThumbY + SCROLLBAR_HEIGHT) {
                this.dragOffsetY = (int) (relativeMouseY - currentThumbY);
            } else {
                this.dragOffsetY = SCROLLBAR_HEIGHT / 2;
                updateScrollFromMouse(relativeMouseY - dragOffsetY);
            }
            return true;
        }

        if (isHovering(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT, mouseX, mouseY)) {
            double relX = mouseX - x;
            double relY = mouseY - y;
            int pixelOffset = Math.round(scrollOffset);
            int row = (int) ((relY - LIST_Y + pixelOffset) / ICON_SIZE);
            int col = (int) ((relX - LIST_X) / ICON_SIZE);
            int slot = row * LIST_COLUMNS + col;

            if (col >= 0 && col < LIST_COLUMNS && slot >= 0 && slot < menu.getNumRecipes()) {
                if (this.minecraft != null && this.minecraft.gameMode != null) {
                    this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, slot);
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            updateScrollFromMouse(mouseY - topPos - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float maxScroll = getMaxScrollPixels();
        if (maxScroll > 0) {
            scrollOffset = clamp(scrollOffset - (float) (verticalAmount * SCROLL_STEP), 0f, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void updateScrollFromMouse(double relativeY) {
        float maxScroll = getMaxScrollPixels();
        if (maxScroll <= 0) return;
        float progress = (float) ((relativeY - LIST_Y) / (double) (LIST_HEIGHT - SCROLLBAR_HEIGHT));
        scrollOffset = clamp(progress, 0f, 1f) * maxScroll;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
