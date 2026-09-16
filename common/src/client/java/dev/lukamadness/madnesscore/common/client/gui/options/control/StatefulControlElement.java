package dev.lukamadness.madnesscore.common.client.gui.options.control;

import dev.lukamadness.madnesscore.common.client.config.structure.StatefulOption;
import dev.lukamadness.madnesscore.common.client.gui.ColorTheme;
import dev.lukamadness.madnesscore.common.client.gui.Layout;
import dev.lukamadness.madnesscore.common.client.gui.widgets.ResetButton;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;

public abstract class StatefulControlElement extends ControlElement {
    protected final ResetButton resetButton;

    public StatefulControlElement(AbstractOptionList list, Dim2i dim, ColorTheme theme) {
        super(list, dim, theme);

        this.resetButton = new ResetButton(this, () -> this.getOption().resetToDefault());
    }

    public boolean isResetOverlayActive() {
        return this.resetButton.isActive();
    }

    public abstract StatefulOption<?> getOption();

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.hovered = this.isMouseOver(mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, delta);

        this.resetButton.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.resetButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected String truncateLabelToFit(String name) {
        int rightReserve = this.isResetOverlayActive() ? this.resetButton.getWidth() : this.getContentWidth() + Layout.OPTION_LABEL_END_PADDING;
        return this.truncateTextToFit(name, this.getWidth() - rightReserve);
    }
}
