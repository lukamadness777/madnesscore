package dev.lukamadness.madnesscore.common.client.gui.widgets;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.api.config.option.OptionImpact;
import dev.lukamadness.madnesscore.common.client.gui.Colors;
import dev.lukamadness.madnesscore.common.client.gui.GuiTint;
import dev.lukamadness.madnesscore.common.client.gui.Layout;
import dev.lukamadness.madnesscore.common.client.gui.options.control.ControlElement;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class ScrollableTooltip {
    private static final ResourceLocation ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/tooltip_arrows.png");
    private static final int ARROW_WIDTH = 5;
    private static final int SPRITE_WIDTH = 10;
    private static final int ARROW_HEIGHT = 9;

    private static final int TEXT_HORIZONTAL_PADDING = Layout.INNER_MARGIN - 1;
    private static final int TEXT_VERTICAL_PADDING = TEXT_HORIZONTAL_PADDING;

    private final Font font = Minecraft.getInstance().font;
    private ControlElement hoveredElement;
    private ScrollbarWidget scrollbar;
    private final Vector2i contentSize = new Vector2i();
    private Dim2i visibleDim;
    private boolean overlayMode;
    private final List<FormattedCharSequence> content = new ArrayList<>();
    private final TooltipParent parent;
    private Dim2i tooltipArea;
    private final Vector2i reservedArea = new Vector2i();

    public ScrollableTooltip(TooltipParent parent) {
        this.parent = parent;
    }

    public interface TooltipParent {
        <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guiEventListener);
        void removeWidget(GuiEventListener guiEventListener);
    }

    public void setTooltipArea(Dim2i area) {
        this.tooltipArea = area;
    }

    public void onControlHover(ControlElement hovered, int mouseX, int mouseY) {
        if (hovered != null) {
            this.hoveredElement = hovered;

            if (this.scrollbar != null) {
                this.parent.removeWidget(this.scrollbar);
                this.scrollbar = null;
            }

            if (this.positionTooltip(false)) {
                this.positionTooltip(true);

                this.scrollbar = this.parent.addRenderableWidget(new ScrollbarWidget(new Dim2i(
                        this.visibleDim.getLimitX() - Layout.SCROLLBAR_WIDTH,
                        this.visibleDim.y(),
                        Layout.SCROLLBAR_WIDTH,
                        this.visibleDim.height()
                ), false, true));
                this.scrollbar.setScrollbarContext(this.visibleDim.height(), this.contentSize.y());
            }
        } else if (this.hoveredElement != null) {
            this.positionTooltip(this.scrollbar != null);

            if ((mouseX < this.hoveredElement.getLimitX() || mouseX >= this.visibleDim.x() ||
                    mouseY < this.hoveredElement.getY() || mouseY >= this.hoveredElement.getLimitY()) &&
                    !this.visibleDim.containsCursor(mouseX, mouseY)) {
                this.hoveredElement = null;

                if (this.scrollbar != null) {
                    this.parent.removeWidget(this.scrollbar);
                    this.scrollbar = null;
                }
            }
        }
    }

    private int getLineHeight() {
        return this.font.lineHeight + Layout.TEXT_LINE_SPACING;
    }

    private int generateTooltipContent(int boxWidth, boolean needsScrolling) {
        int textWidth = boxWidth - TEXT_HORIZONTAL_PADDING * 2;
        if (needsScrolling) {
            textWidth -= Layout.SCROLLBAR_WIDTH;
        }

        var option = this.hoveredElement.getOption();

        this.content.clear();
        this.content.addAll(this.font.split(option.getTooltip(), textWidth));

        OptionImpact impact = option.getImpact();
        if (impact != null) {
            var impactText = Component.translatable("madnesscore.config.options.performance_impact_string", impact.getName());
            this.content.addAll(this.font.split(impactText.withStyle(ChatFormatting.GRAY), textWidth));
        }

        return this.content.size() * this.getLineHeight() - Layout.TEXT_LINE_SPACING + TEXT_VERTICAL_PADDING * 2;
    }

    private boolean positionTooltip(boolean needsScrolling) {
        int defaultBoxWidth = Math.min(this.tooltipArea.getLimitX() - this.tooltipArea.x(), Layout.MAX_TOOLTIP_WIDTH);
        int defaultBoxY = this.hoveredElement.getY();
        int defaultBoxX = this.tooltipArea.x();

        int boxWidth = 0, boxX = 0, boxY = 0;
        boolean fixedBoxY = false;
        int boxYCutoff = this.tooltipArea.getLimitY();

        this.overlayMode = defaultBoxWidth < Layout.MIN_TOOLTIP_WIDTH;

        if (!this.overlayMode) {
            if (this.hoveredElement.getLimitY() < this.reservedArea.y) {
                boxWidth = defaultBoxWidth;
                boxX = defaultBoxX;
                boxY = defaultBoxY;

                boxYCutoff = this.reservedArea.y;
            }

            else if (this.tooltipArea.x() < this.reservedArea.x) {
                int availableWidth = this.reservedArea.x - this.tooltipArea.x();

                if (availableWidth >= Layout.MIN_TOOLTIP_WIDTH) {
                    boxWidth = Math.min(availableWidth, Layout.MAX_TOOLTIP_WIDTH);
                    boxX = defaultBoxX;
                    boxY = defaultBoxY;
                } else {
                    this.overlayMode = true;
                }
            }

            else {
                boxWidth = defaultBoxWidth;
                boxX = defaultBoxX;
                boxY = defaultBoxY;
            }
        }

        if (this.overlayMode) {
            boxWidth = this.hoveredElement.getWidth() - 2 * Layout.TOOLTIP_OUTER_MARGIN;
            boxX = this.hoveredElement.getX() + Layout.TOOLTIP_OUTER_MARGIN;

            int spaceAbove = this.hoveredElement.getY() - this.tooltipArea.y();
            int spaceBelow = this.tooltipArea.getLimitY() - this.hoveredElement.getLimitY() - Layout.TOOLTIP_OUTER_MARGIN;
            if (spaceBelow >= spaceAbove) {
                boxY = this.hoveredElement.getLimitY() + Layout.TOOLTIP_OUTER_MARGIN;
                boxYCutoff = this.tooltipArea.getLimitY() - Layout.TOOLTIP_OUTER_MARGIN;

                fixedBoxY = true;
            } else {
                boxY = this.hoveredElement.getY() - Layout.TOOLTIP_OUTER_MARGIN;
                boxYCutoff = this.hoveredElement.getY() - Layout.TOOLTIP_OUTER_MARGIN;
            }
        }

        int contentHeight = this.generateTooltipContent(boxWidth, needsScrolling);
        int boxYLimit = boxY + contentHeight;

        if (!fixedBoxY) {
            if (boxYLimit > boxYCutoff) {
                boxY -= boxYLimit - boxYCutoff;
            }

            if (boxY < this.tooltipArea.y()) {
                boxY = this.tooltipArea.y();
            }
        }

        this.contentSize.set(boxWidth, contentHeight);

        int maxVisibleHeight = boxYCutoff - boxY;
        int visibleHeight = Math.min(contentHeight, maxVisibleHeight);
        this.visibleDim = new Dim2i(boxX, boxY, boxWidth, visibleHeight);

        return contentHeight > maxVisibleHeight;
    }

    public void render(@NotNull GuiGraphics graphics) {
        if (this.hoveredElement == null) {
            return;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 0.0f, 400.0f);
        try {
            this.renderInternal(graphics);
        } finally {
            graphics.pose().popPose();
        }
    }

    private void renderInternal(@NotNull GuiGraphics graphics) {
        if (!this.overlayMode) {
            int arrowX = this.visibleDim.x() - ARROW_WIDTH;
            int arrowY = this.hoveredElement.getCenterY() - (ARROW_HEIGHT / 2);

            arrowY = Math.max(arrowY, this.tooltipArea.y());
            int arrowYConstrained = Math.min(arrowY + ARROW_HEIGHT, this.tooltipArea.getLimitY()) - ARROW_HEIGHT;

            GuiTint.withTint(Colors.BACKGROUND_LIGHT, () ->
                    graphics.blit(ARROW_TEXTURE, arrowX, arrowYConstrained, ARROW_WIDTH, 0, ARROW_WIDTH, ARROW_HEIGHT, SPRITE_WIDTH, ARROW_HEIGHT));
            GuiTint.withTint(Colors.BACKGROUND_DEFAULT, () ->
                    graphics.blit(ARROW_TEXTURE, arrowX, arrowYConstrained, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, SPRITE_WIDTH, ARROW_HEIGHT));
        }

        int lineHeight = this.getLineHeight();

        int scrollAmount = 0;
        if (this.scrollbar != null) {
            scrollAmount = this.scrollbar.getScrollAmount();
        }

        var backgroundColor = this.overlayMode ? Colors.BACKGROUND_OVERLAY : Colors.BACKGROUND_LIGHT;

        graphics.enableScissor(this.visibleDim.x(), this.visibleDim.y(), this.visibleDim.getLimitX(), this.visibleDim.getLimitY());
        graphics.fill(this.visibleDim.x(), this.visibleDim.y(), this.visibleDim.getLimitX(), this.visibleDim.getLimitY(), backgroundColor);
        for (int i = 0; i < this.content.size(); i++) {
            graphics.drawString(this.font, this.content.get(i),
                    this.visibleDim.x() + TEXT_HORIZONTAL_PADDING, this.visibleDim.y() + TEXT_VERTICAL_PADDING + (i * lineHeight) - scrollAmount,
                    Colors.FOREGROUND);
        }
        graphics.disableScissor();
    }

    public boolean mouseScrolled(double d, double e, double amount) {
        if (this.visibleDim != null && this.visibleDim.containsCursor(d, e) && this.scrollbar != null) {
            this.scrollbar.scroll((int) (-amount * 10));
            return true;
        }
        return false;
    }

    public void setReservedAreaTopLeftCorner(int x, int y) {
        this.reservedArea.set(x - Layout.TOOLTIP_OUTER_MARGIN, y - Layout.TOOLTIP_OUTER_MARGIN);
    }
}
