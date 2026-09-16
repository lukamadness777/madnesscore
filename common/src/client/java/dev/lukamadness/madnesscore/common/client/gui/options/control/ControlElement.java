package dev.lukamadness.madnesscore.common.client.gui.options.control;

import dev.lukamadness.madnesscore.common.client.config.structure.Option;
import dev.lukamadness.madnesscore.common.client.gui.ColorTheme;
import dev.lukamadness.madnesscore.common.client.gui.Colors;
import dev.lukamadness.madnesscore.common.client.gui.Layout;
import dev.lukamadness.madnesscore.common.client.gui.widgets.AbstractWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public abstract class ControlElement extends AbstractWidget {
    protected final AbstractOptionList list;
    protected final ColorTheme theme;

    public ControlElement(AbstractOptionList list, Dim2i dim, ColorTheme theme) {
        super(dim);
        this.list = list;
        this.theme = theme;
    }

    public abstract Option getOption();

    public int getContentWidth() {
        return this.getOption().getControl().getMaxWidth();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        String name = this.getOption().getName().getString();

        if (this.getOption().isEnabled() && this.getOption().hasChanged()) {
            name = name + " *";
        }

        name = this.truncateLabelToFit(name);

        String label;
        if (this.getOption().isEnabled()) {
            if (this.getOption().hasChanged()) {
                label = ChatFormatting.ITALIC + name;
            } else {
                label = ChatFormatting.WHITE + name;
            }
        } else {
            label = String.valueOf(ChatFormatting.GRAY) + ChatFormatting.STRIKETHROUGH + name;
        }

        this.hovered = this.isMouseOver(mouseX, mouseY);

        this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), this.hovered ? Colors.BACKGROUND_HOVER : Colors.BACKGROUND_LIGHT);
        this.drawString(graphics, label, this.getX() + Layout.OPTION_TEXT_SIDE_PADDING, this.getCenterY() + Layout.REGULAR_TEXT_BASELINE_OFFSET, Colors.FOREGROUND);

        if (this.isFocused()) {
            this.drawBorder(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), -1);
        }
    }

    protected MutableComponent formatDisabledControlValue(Component value) {
        return value.copy().withStyle(Style.EMPTY
                .withColor(ChatFormatting.GRAY)
                .withItalic(true)
                .withStrikethrough(true));
    }

    protected String truncateLabelToFit(String name) {
        return this.truncateTextToFit(name, this.getWidth() - this.getContentWidth() - Layout.OPTION_LABEL_END_PADDING);
    }

    @Override
    public int getY() {
        return super.getY() - this.list.getScrollAmount();
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (!this.getOption().isEnabled()) {
            return null;
        }
        return super.nextFocusPath(event);
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}
