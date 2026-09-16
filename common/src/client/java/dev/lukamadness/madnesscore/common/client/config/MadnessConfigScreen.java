package dev.lukamadness.madnesscore.common.client.config;

import dev.lukamadness.madnesscore.common.client.config.structure.ModOptions;
import dev.lukamadness.madnesscore.common.client.config.structure.Option;
import dev.lukamadness.madnesscore.common.client.config.structure.OptionPage;
import dev.lukamadness.madnesscore.common.client.config.structure.Page;
import dev.lukamadness.madnesscore.common.client.gui.Dimensioned;
import dev.lukamadness.madnesscore.common.client.gui.GuiTint;
import dev.lukamadness.madnesscore.common.client.gui.Layout;
import dev.lukamadness.madnesscore.common.client.gui.options.control.ControlElement;
import dev.lukamadness.madnesscore.common.client.gui.widgets.AbstractWidget;
import dev.lukamadness.madnesscore.common.client.gui.widgets.KeyBoundButtonWidget;
import dev.lukamadness.madnesscore.common.client.gui.widgets.OptionListWidget;
import dev.lukamadness.madnesscore.common.client.gui.widgets.PageListWidget;
import dev.lukamadness.madnesscore.common.client.gui.widgets.ScrollableTooltip;
import dev.lukamadness.madnesscore.common.client.gui.widgets.SearchWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class MadnessConfigScreen extends Screen implements Dimensioned, ScrollableTooltip.TooltipParent {
    private final Screen prevScreen;

    private Dim2i dim;
    private boolean insetX, insetY;

    private PageListWidget pageList;
    private SearchWidget searchWidget;
    private OptionListWidget optionList;

    private KeyBoundButtonWidget applyButton, closeButton, undoButton;
    private List<KeyBoundButtonWidget> shortcutButtons = List.of();

    private boolean hasPendingChanges;

    private final ScrollableTooltip tooltip = new ScrollableTooltip(this);

    public MadnessConfigScreen(Screen prevScreen) {
        super(Component.translatable("madnesscore.config.title"));
        this.prevScreen = prevScreen;

        ConfigManager.CONFIG.resetAllOptionsFromBindings();
    }

    @Override
    protected void init() {
        super.init();

        ConfigManager.CONFIG.invalidateGlobalRebuildDependents();
        this.rebuild();
    }

    private int ifInsetX(int value) {
        return this.insetX ? value : 0;
    }

    private int ifInsetY(int value) {
        return this.insetY ? value : 0;
    }

    private int ifNotInsetX(int value) {
        return this.insetX ? 0 : value;
    }

    private int ifNotInsetY(int value) {
        return this.insetY ? 0 : value;
    }

    private void rebuild() {
        this.clearWidgets();

        this.updateScreenDimensions();
        var x = this.getX();
        var y = this.getY();
        var w = this.getWidth();
        var h = this.getHeight();

        int topBarHeight = Layout.BUTTON_SHORT;
        this.searchWidget = new SearchWidget(this::onSearchResults, new Dim2i(x, y, w, topBarHeight));

        int topBarClear = topBarHeight + this.ifInsetY(Layout.INNER_MARGIN);
        this.pageList = new PageListWidget(new Dim2i(x, y + topBarClear, Layout.PAGE_LIST_WIDTH, h - topBarClear), this);
        this.addRenderableWidget(this.pageList);

        boolean stackVertically = false;
        boolean reserveBottomSpace = false;

        int minWidthToStack = Layout.PAGE_LIST_WIDTH + Layout.INNER_MARGIN * 2 + Layout.OPTION_WIDTH + Layout.OPTION_LIST_SCROLLBAR_OFFSET + Layout.SCROLLBAR_WIDTH + Layout.BUTTON_LONG;
        int maxWidthToStack = minWidthToStack + Layout.BUTTON_LONG * 2 + Layout.INNER_MARGIN;

        if (w > minWidthToStack && w < maxWidthToStack) {
            stackVertically = true;
        } else if (w < minWidthToStack) {
            reserveBottomSpace = true;
        }

        this.rebuildActionButtons(stackVertically);

        this.addRenderableWidget(this.searchWidget);
        this.searchWidget.updateWidgetWidth(this.getWidth());

        var optionListDim = new Dim2i(
                this.pageList.getLimitX(),
                y + topBarHeight + Layout.INNER_MARGIN,
                Layout.OPTION_WIDTH + Layout.OPTION_LIST_SCROLLBAR_OFFSET + Layout.SCROLLBAR_WIDTH,
                h - topBarHeight - (reserveBottomSpace ? (Layout.INNER_MARGIN * 2 + Layout.BUTTON_SHORT) : Layout.INNER_MARGIN) - this.ifNotInsetY(Layout.INNER_MARGIN)
        );
        this.optionList = new OptionListWidget(this, optionListDim, this::onSectionFocused);
        this.addRenderableWidget(this.optionList);

        var tooltipAreaY = y + topBarHeight + this.ifInsetY(Layout.TOOLTIP_OUTER_MARGIN);
        this.tooltip.setTooltipArea(
                new Dim2i(
                        this.optionList.getLimitX(),
                        tooltipAreaY,
                        this.getLimitX() - this.optionList.getLimitX() - this.ifNotInsetX(Layout.TOOLTIP_OUTER_MARGIN),
                        this.getLimitY() - tooltipAreaY - this.ifNotInsetY(Layout.TOOLTIP_OUTER_MARGIN)
                )
        );
    }

    private void rebuildActionButtons(boolean stackVertically) {
        int buttonW = Layout.BUTTON_LONG;
        int buttonH = Layout.BUTTON_SHORT;
        int closeX = this.getLimitX() - buttonW - this.ifNotInsetX(Layout.INNER_MARGIN);
        int closeY = this.getLimitY() - (this.ifNotInsetY(Layout.INNER_MARGIN) + buttonH);

        int dx = stackVertically ? 0 : -(Layout.INNER_MARGIN + buttonW);
        int dy = stackVertically ? -(Layout.INNER_MARGIN + buttonH) : 0;
        int actionRowX = closeX + dx;
        int actionRowY = stackVertically ? closeY + dy : this.getLimitY() - (Layout.INNER_MARGIN + buttonH);

        this.closeButton = new KeyBoundButtonWidget(new Dim2i(closeX, closeY, buttonW, buttonH), Component.translatable("gui.done"), this::onClose, true, false, GLFW.GLFW_KEY_D);
        this.applyButton = new KeyBoundButtonWidget(new Dim2i(actionRowX, actionRowY, buttonW, buttonH), Component.translatable("madnesscore.config.buttons.apply"), ConfigManager.CONFIG::applyAllOptions, true, false, GLFW.GLFW_KEY_A);
        this.undoButton = new KeyBoundButtonWidget(new Dim2i(actionRowX + dx, actionRowY + dy, buttonW, buttonH), Component.translatable("madnesscore.config.buttons.undo"), this::undoChanges, true, false, GLFW.GLFW_KEY_U);

        this.addRenderableWidget(this.closeButton);
        this.addRenderableWidget(this.undoButton);
        this.addRenderableWidget(this.applyButton);
        this.shortcutButtons = List.of(this.closeButton, this.applyButton, this.undoButton);
    }

    private void updateScreenDimensions() {
        var baseContentWidth = Layout.PAGE_LIST_WIDTH + Layout.INNER_MARGIN + Layout.OPTION_WIDTH + Layout.OPTION_LIST_SCROLLBAR_OFFSET + Layout.SCROLLBAR_WIDTH + Layout.TOOLTIP_OUTER_MARGIN;
        var minContentWidth = baseContentWidth + (Layout.MAX_TOOLTIP_WIDTH - Layout.MIN_TOOLTIP_WIDTH) / 2 + Layout.MIN_TOOLTIP_WIDTH;
        var maxContentWidth = baseContentWidth + Layout.MAX_TOOLTIP_WIDTH;
        var maxInterpolatingBorderWidth = 100;
        var widthInterpolationStart = minContentWidth + Layout.CONTENT_BORDER_MIN_WIDTH;
        var widthInterpolationEnd = maxContentWidth + maxInterpolatingBorderWidth;

        int contentWidth = this.width;
        this.insetX = false;
        if (this.width > minContentWidth + Layout.CONTENT_BORDER_MIN_WIDTH) {
            if (this.width < widthInterpolationEnd) {
                float t = (float) (this.width - widthInterpolationStart) / (widthInterpolationEnd - widthInterpolationStart);
                contentWidth = minContentWidth + (int) (t * (maxContentWidth - minContentWidth));
            } else {
                contentWidth = maxContentWidth;
            }
            this.insetX = true;
        }

        int contentHeight = this.height;
        this.insetY = false;
        if (this.height > Layout.CONTENT_MIN_HEIGHT + Layout.CONTENT_BORDER_HEIGHT && this.insetX) {
            contentHeight = this.height - Layout.CONTENT_BORDER_HEIGHT;
            this.insetY = true;
        }

        this.dim = new Dim2i(
                (this.width - contentWidth) / 2,
                (this.height - contentHeight) / 2,
                contentWidth,
                contentHeight
        );
    }

    private void onSearchResults(List<Option.OptionNameSource> searchResults) {
        if (searchResults.isEmpty()) {
            this.optionList.clearFilter();
        } else {
            this.optionList.setFilteredOptions(searchResults);
        }
        this.optionList.rebuild(this);
    }

    private void onSectionFocused(Page page) {
        this.pageList.switchSelected(page);
    }

    public void jumpToPage(Page page) {
        if (this.optionList != null) {
            this.optionList.jumpToPage(page);
        }
    }

    public void filterToPage(ModOptions modOptions, Page page) {
        if (this.optionList == null || !(page instanceof OptionPage optionPage)) {
            return;
        }

        this.optionList.setFilteredOptions(optionPage.collectSources(modOptions));
        this.optionList.rebuild(this);
        this.optionList.resetScroll();
    }

    public void showAllPages(Page jumpTo) {
        if (this.optionList == null) {
            return;
        }

        this.optionList.clearFilter();
        this.optionList.rebuild(this);
        if (jumpTo != null) {
            this.optionList.jumpToPage(jumpTo);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.updateControls(mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, delta);

        this.tooltip.render(graphics);
    }

    private void updateControls(int mouseX, int mouseY) {
        boolean hasChanges = ConfigManager.CONFIG.anyOptionChanged();

        this.applyButton.setEnabled(hasChanges);
        this.undoButton.setVisible(hasChanges);
        this.closeButton.setEnabled(!hasChanges);

        AbstractWidget reservedAreaBlocker = hasChanges ? this.undoButton : this.applyButton;
        this.tooltip.setReservedAreaTopLeftCorner(reservedAreaBlocker.getX(), reservedAreaBlocker.getY());

        this.hasPendingChanges = hasChanges;

        ControlElement hovered = null;
        ControlElement focused = null;
        if (mouseX >= this.optionList.getX() && mouseX <= this.optionList.getLimitX() &&
                mouseY >= this.optionList.getY() && mouseY <= this.optionList.getLimitY()) {
            for (ControlElement element : this.optionList.getControls()) {
                if (element.isMouseOver(mouseX, mouseY)) {
                    hovered = element;
                    break;
                }
                if (element.isFocused()) {
                    focused = element;
                }
            }
        }
        var hoverTarget = hovered != null ? hovered : focused;

        this.tooltip.onControlHover(hoverTarget, mouseX, mouseY);
    }

    private void undoChanges() {
        ConfigManager.CONFIG.resetAllOptionsFromBindings();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchWidget.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!this.searchWidget.isSearching() && keyCode == GLFW.GLFW_KEY_T) {
            this.setFocused(this.searchWidget);
            return true;
        }

        for (var button : this.shortcutButtons) {
            if (button.tryActivateShortcut(keyCode, modifiers)) {
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.hasPendingChanges) {
                this.undoChanges();
            }

            this.onClose();
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!super.mouseClicked(mouseX, mouseY, button)) {
            if (!this.searchWidget.isFocused()) {
                this.setFocused(this.searchWidget);
                return true;
            }
            this.setFocused(null);
            return true;
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double f, double amount) {
        if (this.tooltip.mouseScrolled(x, y, amount)) {
            return true;
        }

        return super.mouseScrolled(x, y, f, amount);
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guiEventListener) {
        return super.addRenderableWidget(guiEventListener);
    }

    @Override
    public void removeWidget(GuiEventListener guiEventListener) {
        super.removeWidget(guiEventListener);
    }

    public <T extends GuiEventListener & Renderable & NarratableEntry> void setWidgetPresence(T guiEventListener, boolean present) {
        this.removeWidget(guiEventListener);
        if (present) {
            this.addRenderableWidget(guiEventListener);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !this.hasPendingChanges;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.prevScreen);
    }

    @Override
    public Dim2i getDimensions() {
        return this.dim;
    }

    public static int renderIconWithSpacing(GuiGraphics graphics, ResourceLocation icon, int color, boolean iconMonochrome, int x, int y, int height, int margin) {
        int iconSize = height - margin * 2;

        final int blitX = x + margin;
        final int blitY = y + height / 2 - iconSize / 2;
        if (iconMonochrome) {
            GuiTint.withTint(color, () -> graphics.blit(icon, blitX, blitY, iconSize, iconSize, 0.0f, 0.0f, 16, 16, 16, 16));
        } else {
            GuiTint.noTint(() -> graphics.blit(icon, blitX, blitY, iconSize, iconSize, 0.0f, 0.0f, 16, 16, 16, 16));
        }

        return margin * 2 + iconSize;
    }
}
